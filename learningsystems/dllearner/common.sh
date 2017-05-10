#!/bin/bash - 
#===============================================================================
#
#          FILE: common.sh
# 
#         USAGE: . ./common.sh 
# 
#   DESCRIPTION: File contains common functions to be used by the run and
#                validate scripts. This file should not be executed but rather
#                be sourced from run and validate
# 
#===============================================================================

set -o nounset                              # Treat unset variables as an error

settings_key="settings"
settings_prefix="smlb_settings__"
learning_tasks_dir_name="learningtasks"
kr_lang_dir_name="owl"
learning_problems_dir_name="lp"
data_dir_name="data"
lp_specific_condif_file_name="dllearner.conf"
dllearner_executable_name="cli"

# the following constants should be used as sections in a configuration file
algorithm_const="algorithm"
lp_const="learningproblem"
measure_const="measure"
reasoner_const="reasoner"

algorithm_is_set=0
lp_is_set=0
measure_is_set=0
reasoner_is_set=0


#---  FUNCTION  ----------------------------------------------------------------
#          NAME:  debug
#   DESCRIPTION:  Writes debug output if log level is set accordingly.
#    PARAMETERS:  The message to log.
#-------------------------------------------------------------------------------
debug() {
    # if  smlb_debug is not unset  and  smlb_debug is greater 0
    if [ ! -z "${smlb_log_level+'foo'}" ] && [ "$smlb_log_level" -gt 0 ]
    then
        echo "DEBUG: $1"
    fi
}


#---  FUNCTION  ----------------------------------------------------------------
#          NAME:  trace
#   DESCRIPTION:  Writes trace output if log level is set accordingly.
#    PARAMETERS:  The message to log.
#-------------------------------------------------------------------------------
trace() {
    # if  smlb_debug is not unset  and  smlb_debug is greater 0
    if [ ! -z "${smlb_log_level+'foo'}" ] && [ "$smlb_log_level" -gt 1 ]
    then
        echo "TRACE: $1"
    fi
}


#---  FUNCTION  ----------------------------------------------------------------
#          NAME:  find_dllearner
#   DESCRIPTION:  Tries to find the DL-Learner executable inside the directory
#                 where this shell script is located.
#    PARAMETERS:  None
#       RETURNS:  The path to the DL-Learner executable
#-------------------------------------------------------------------------------
find_dllearner() {
    set -- */bin/"$dllearner_executable_name" bin/"$dllearner_executable_name"
    echo "$1"
}


#---  FUNCTION  ----------------------------------------------------------------
#          NAME:  set_variable
#   DESCRIPTION:  Sets the environment variable with key $1 and value $2. Will
#                 also take care of
#                 - prefixing the key and replacing dots in the key with
#                   underscores, s.t. e.g. settings.algorithm.type will become
#                   smlb_settings__algorithm__type
#                 - escaping the value correctly, i.e. whenever the quotes are
#                   important, e.g. in case of DL-Learner settings like "celoe"
#                   they will be escaped
#    PARAMETERS:  $1: The key, e.g. settings.algorithm.type
#                 $2: The value, e.g. "celoe"
#-------------------------------------------------------------------------------
set_variable() {
    key=${1//./__}  # replace dots with two underscores
    value=$2

    # if    key starts with 'settings'
    if [ "${key#$settings_key}" != "$key" ]
    then
        trace "$key is a settings key"
        value=${value//\"/\\\"}  # escape quotes
        value=${value//\ /\\\ }  # escape spaces
    else
        trace "$key is not a settings key"
    fi
    eval "smlb_$key=$value"
}


#---  FUNCTION  ----------------------------------------------------------------
#          NAME:  remove_property_file_blanks
#   DESCRIPTION:  Removes empty and comment lines in a property file.
#    PARAMETERS:  None; reads from stdin and writes to stdout; can thus be used
#                 with pipes
#       RETURNS:  The input property file content with all empty and comment
#                 lines removed
#-------------------------------------------------------------------------------
remove_property_file_blanks() {
    grep -v '^\s*\(#\|\!\|\s*$\)'
}


#---  FUNCTION  ----------------------------------------------------------------
#          NAME:  remove_ini_file_blanks
#   DESCRIPTION:  Removes empty and comment lines in an INI file.
#    PARAMETERS:  None; reads from stdin and writes to stdout; can thus be used
#                 with pipes
#       RETURNS:  The input INI file content with all empty and comment lines
#                 removed
#-------------------------------------------------------------------------------
remove_ini_file_blanks() {
    grep -v '^\s*\(;\|\s*$\)'
}


#---  FUNCTION  ----------------------------------------------------------------
#          NAME:  trim
#   DESCRIPTION:  Strips off leading and trailing whitespaces
#    PARAMETERS:  None; reads from stdin and writes to stdout; can thus be used
#                 with pipes
#       RETURNS:  The input line with all leading and trailing whitespaces
#                 removed
#-------------------------------------------------------------------------------
trim() {
    sed 's/^\s*//g' | sed 's/\s*$//g'
}


#---  FUNCTION  ----------------------------------------------------------------
#          NAME:  quote_examples
#   DESCRIPTION:  Puts example URIs in quotes and adds commas between them.
#    PARAMETERS:  None; reads from stdin and writes to stdout; can thus be used
#                 with pipes
#       RETURNS:  The input examples, put in quotes and separated by commas
#-------------------------------------------------------------------------------
quote_examples() {
    sed -e 's.^.".' -e 's.$.",.' | sed -e '$ s/.$//'
}


#---  FUNCTION  ----------------------------------------------------------------
#          NAME:  read_examples
#   DESCRIPTION:  Reads examples from file given by $1
#    PARAMETERS:  The path to the examples file to read
#       RETURNS:  String containing the quoted and comma separated examples.
#-------------------------------------------------------------------------------
read_examples() {
    # remove blank & comment lines
    grep -v '^\s*\(;\|\s*$\)' "$1" | quote_examples
}


#---  FUNCTION  ----------------------------------------------------------------
#          NAME:  read_property_file
#   DESCRIPTION:  Read in a property file, i.e. something that looks like this:
#                 
#                   filename.workdir = /home/foo/bar/dllearner-1/train
#                   filename.pos = /home/foo/bar/dllearner-1/train/pos.txt
#                   filename.neg = /home/foo/bar/dllearner-1/train/neg.txt
#                   filename.output = /home/foo/bar/dllearner-1/train/train.out
#                   main.learningtask = premierleague
#                   main.learningproblem = 1
#                   main.maxExecutionTime = 500
#                   settings.algorithm.type = "celoe"
#                 
#                 This results in a bunch of variables named like the keys in
#                 the property file except that
#                 
#                 - they are prefixed with smlb_
#                 - dots (.) are replaced by two underscores (__) (since dots are
#                   not allowed in variable names)
#                 
#                 Thus filename.workdir will be smlb_filename_workdir.
#
#    PARAMETERS:  The path to the property file to read
#       RETURNS:  Nothing
#-------------------------------------------------------------------------------
read_property_file() {
    # The `sed 's/ /@@/g'` is used to 'escape' whitespace within one
    # configuration option. Without this the line
    # type="closed world reasoner"
    # would be processed in three steps: 'type="closed', 'world' and
    # 'reasoner"'.
    # The replacement of whitespaces with @@ is reverted inside the loop.
    for conf_option in $(cat "$@" | remove_property_file_blanks | sed 's/\s/@@/g')
    do
        # $conf_option now is something like
        #
        #  settings.algorithm.type@@=@@"celoe"

        # extract
        # - key1, which determines whether the configuration line refers to
        #   1) main settings ('main')
        #   2) file name settings ('filenames')
        #   3) DL-Learner settings ('settings')
        # - key2, which is mainly used in case 3) above to check for
        #   DL-Learner component declarations, i.e. to be able to handle
        #   situations where an algorithm, learning problem, ... is declared.
        #   This is of importance since we will have to provide default
        #   settings for all components that have not been declared
        #   explicitly.
        # - key3, which is again mainly used in case 3) above. As in the example
        #   
        #    settings.algorithm.type@@=@@"celoe"
        #
        #   a component is declared only if key3 equals 'type'.
        whole_key=$(echo "$conf_option" | cut -d= -f1 | sed 's/@@//g')
        key1=$(echo "$whole_key" | cut -d. -f1 | trim)
        value=$(echo "$conf_option" | cut -d= -f2 | sed 's/@@/ /g' | trim)

        if [ "$key1" = "$settings_key" ]
        then
            # handle DL-Learner settings, i.e. check whether certain components
            # were declared

            # Since we're looking at a DL-Learner setting line there will
            # always be a key2 and key3
            key2=$(echo "$whole_key" | cut -d. -f2 | trim)
            key3=$(echo "$whole_key" | cut -d. -f3 | trim)

            if [ "$key3" = "type" ]
            then
                # check whether this configuration line declared one of the
                # components that that require a default setting, i.e. a
                # - learning algorithm
                # - learning problem
                # - reasoner component
                # - measure
                if [ "$key2" = "$algorithm_const" ]
                then
                    debug "Learning algorithm set to $value in $1"
                    algorithm_is_set=1
                elif [ "$key2" = "$lp_const" ]
                then
                    debug "Learning problem set to $value in $1"
                    lp_is_set=1
                elif [ "$key2" = "$measure_const" ]
                then
                    debug "Measure set to $value in $1"
                    measure_is_set=1
                elif [ "$key2" = "$reasoner_const" ]
                then
                    debug "Reasoner component set to $value in $1"
                    reasoner_is_set=1
                fi
            fi
        fi
        
        set_variable "$whole_key" "$value"
    done
}


#---  FUNCTION  ----------------------------------------------------------------
#          NAME:  read_lp_ini_file
#   DESCRIPTION:  Reads settings from a learning problem-specific configuration
#                 file. Such files are assumed to be in the INI format and
#                 usually only contain the estimated loading time for the
#                 DL-Learner  which will be used elsewhere to compute the
#                 maximum execution time:
#                 
#                   [main]
#                   ; time required for loading, materialising, etc.
#                   loadingTime = 15
#                 
#                 Besides this, such INI files might also contain any
#                 DL-Learner-specific settings, e.g.
#                 
#                   [main]
#                   ; time required for loading, materialising, etc.
#                   loadingTime = 15
#                   algorithm.type = "eltl"
#                   algorithm.maxClassExpressionDepth = 5
#                   algorithm.maxNrOfResults = 20
#                 
#                 However, we are currently thinking about declaring such a
#                 usage as deprecated. Besides the fact that we cannot really
#                 think of a usage scenario where such learning
#                 problem-specific settings make sense it mixes the DL-Learner
#                 configuration (e.g. algorithm.type = "eltl") with runtime
#                 settings (e.g. loadingTime = 15) which might cause key
#                 clashes.
#    PARAMETERS:  The file path to the INI file
#-------------------------------------------------------------------------------
read_lp_ini_file() {
    # Basic assumtions to distinguish between runtime settings and DL-Learner
    # configuration:
    # - Everything is under [main]
    # - DL-Learner configuration entries contain nested keys, i.e. keys which
    #   contain dots
    # - Everything else is a runtime configuration setting

    # The `sed 's/ /@@/g'` is used to 'escape' whitespace within one
    # configuration option. Without this the line
    # type="closed world reasoner"
    # would be processed in three steps: 'type="closed', 'world' and
    # 'reasoner"'.
    # The replacement of whitespaces with @@ is reverted inside the loop.
    for conf_option in $(sed 's/\s/@@/g' "$@" |\
            remove_ini_file_blanks |\
            awk -F ' *= *' '{ if ($1 ~ /^\[/) section=$1; else if ($1 !~ /^$/) print section"|"$1 "=" $2 }')
    do
        # $conf_option's of the second example above will now look like:
        # 
        #   [main]|loadingTime@@=@@15
        #   [main]|algorithm.type@@=@@"eltl"
        #   [main]|algorithm.maxClassExpressionDepth@@=@@5
        #   [main]|algorithm.maxNrOfResults@@=@@20
        #
        # where $conf_option will hold one line at each iteration.

        # strip off the section part ([main] in the example above)
        key1=$(echo "$conf_option" | sed 's/@@/ /g' | cut -d'|' -f1)
        key1_length=$((${#key1} - 2))  # key length without brackets
        key1=$(echo "$key1" | cut -c 2-$((key1_length+1)))

        # key2 is the actual key inside a section marker like [main]
        key2=$(echo "$conf_option" | sed 's/@@/ /g' | cut -d'|' -f2 | cut -d"=" -f1 | trim)
        val=$(echo "$conf_option" | sed 's/@@/ /g' | cut -d'|' -f2 | cut -d"=" -f2 | trim)

        contains_dot=$(echo "$key2" | grep -c '\.')
        if [ "$contains_dot" -gt 0 ]
        then
            ###############################################
            # DL-Learner configuration

            # Check whether we are in the validation phase. If so, no learning
            # algorithms can be set, since the validation is performed by
            # a dedicated learning algorithm to be set later.
            #                                        $key2 starts with $algorithm_const
            if [ "$smlb_step" != "validate" ] || [ "${key2#$algorithm_const}" = "$key2" ]
            then
                # check if there is already a corresponing variable
                target_key=$settings_prefix${key2//./__}
                exists=$(set | grep -c "^$target_key")
                if [ "$exists" -eq 0 ]
                then
                    val=${val//\"/\\\"}  # escape quotes
                    val=${val//\ /\\\ }  # escape spaces
                    trace "$target_key not set, yet. Will be set to $val"
                    eval "$target_key=$val"
                else
                    trace "$target_key already set. Skipping..."
                fi
            fi
        else
            ###############################################
            # runtime configuration
            eval "smlb_$key2=$val"
        fi
    done
}


#---  FUNCTION  ----------------------------------------------------------------
#          NAME:  print_smlb_env_vars
#   DESCRIPTION:  Prints all variables starting with smlb_ prefix
#    PARAMETERS:  None
#       RETURNS:  Nothing
#-------------------------------------------------------------------------------
print_smlb_env_vars() {
    set | grep "^smlb"
}


#---  FUNCTION  ----------------------------------------------------------------
#          NAME:  write_config
#   DESCRIPTION:  Writes all settings variables (i.e. variables starting with
#                 smlb_settings__) to the file given as $1
#    PARAMETERS:  The config file to write to
#       RETURNS:  Nothing
#-------------------------------------------------------------------------------
write_config() {
    > "$1"

    for setting in $(set | grep "^$settings_prefix" | sed 's/ /@@/g')
    do
        # $setting looks like smlb_settings__algorithm__type='"celoe"'
        # cut off the smlb_settings__ part 
        setting_var=$(echo "$setting" | cut -d= -f1 | trim)
        config_key="${setting_var#$settings_prefix}"  # cut off settings prefix
        config_key="${config_key//__/.}"  # replace underscores with dots again
        value="${value//@@/ /}"
        value=$(eval "echo \$$setting_var")
        trace "Writing $config_key = $value to $1"
        echo "$config_key = $value" >> "$1"
    done
}


#---  FUNCTION  ----------------------------------------------------------------
#          NAME:  add_to_config
#   DESCRIPTION:  Adds a line given as $1 to the config file given by $2
#    PARAMETERS:  $1: The line to add to the configuration file
#                 $2: The configuration file
#       RETURNS:  Nothing
#-------------------------------------------------------------------------------
add_to_config() {
    config_line=$1
    config_file=$2
    echo "$config_line" >> "$config_file"
}


#---  FUNCTION  ----------------------------------------------------------------
#          NAME:  add_knowledge_sources_to_config_and_get_sources
#   DESCRIPTION:  Collects all references to OWL knowledge source files and
#                 adds them to the config file provided via $2; returns the
#                 collected knowledge sources as string
#    PARAMETERS:  $1: The learning task to work on
#                 $2: The config file to write to
#       RETURNS:  Nothing; but echos a string containing all knowledge source
#                 references
#-------------------------------------------------------------------------------
add_knowledge_sources_to_config_and_get_sources() {
    learning_task=$1
    config_file=$2

    data_dir="../../$learning_tasks_dir_name/$learning_task/$kr_lang_dir_name/$data_dir_name"
    sources=
    i=1
    for f in $data_dir/*.owl
    do
        add_to_config "ks$i.type = \"OWL File\"" "$config_file"
        add_to_config "ks$i.fileName = \"$program_dir/$f\"" "$config_file"

        sources="$sources${sources:+, }ks$i"
        _=$((i=i+1))
    done

    echo $sources
}
