#!/bin/sh
tool_name=dllearner

learning_task_dir_name=learningtasks
owl_dir_name=owl
data_dir_name=data
lp_dir_name=lp
dllearner_executable_name=cli
# the following constants should be used as sections in the configuration INI file
reasoner_const="reasoner"
algorithm_const="algorithm"
heuristic_const="heuristic"
operator_const="refinementoperator"
lp_const="learningproblem"
lp_param_const="learningparameter"
measure_const="measure"
cli_const="cli"
structurelearner_const="structurelearner"
algorithm_is_set=0
lp_is_set=0
reasoner_is_set=0
measure_is_set=0

find_dllearner() {
    set -- */bin/"$dllearner_executable_name" bin/"$dllearner_executable_name"
    echo "$1"
}

log() {
    echo "$@" >&2
}

add_conf() {
    echo "$@" >>"$conf"
}

remove_blanks() {
    grep -v '^\s*\(;\|\s*$\)'
}

quote_examples() {
    sed $prefix_sed_cmd -e 's.^.".' -e 's.$.",.' | sed -e '$ s/.$//'
}

read_lp_conf() {
    # The `sed 's/ /@@/g'` is used to 'escape' whitespace within one
    # configuration option. Without this the line
    # type="closed world reasoner"
    # would be processed in three steps: 'type="closed', 'world' and
    # 'reasoner"'.
    # The replacement of whitespaces with @@ is reverted inside the loop.
    for conf_option in $(sed 's/ /@@/g' $@ | grep -v -e "^;" | awk -F ' *= *' '{ if ($1 ~ /^\[/) section=$1; else if ($1 !~ /^$/) print section"|"$1 "=" $2 }')
    do
        key1=`echo $conf_option | sed 's/@@/ /g' | cut -d'|' -f1`
        len=$((`expr length $key1` - 2))
        key1=`expr substr $key1 2 $len`
        key2=`echo $conf_option | sed 's/@@/ /g' | cut -d'|' -f2 | cut -d"=" -f1`
        val=`echo $conf_option | sed 's/@@/ /g' | cut -d'|' -f2 | cut -d"=" -f2`
        
        if [ $key1 != "main" ] # skip all entries under [main] section
        then
            # Mark the main component types as 'set'. If, e.g. there was no
            # reasoner component configured explicitly in the given
            # configuration a fallback/default reasoner will be set later.
            # Thus we have to keep track whether a 
            #
            #   [reasoner]
            #   type = "xyz"
            #
            # already appeared in the read config file.
            if [ $key2 = "type" ]
            then
                if [ $key1 = $algorithm_const ]
                then
                    algorithm_is_set=1
                
                elif [ $key1 = $lp_const ]
                then
                    lp_is_set=1
                
                elif [ $key1 = $reasoner_const ]
                then
                    reasoner_is_set=1

                elif [ $key1 = $measure_const ]
                then
                    measure_is_set=1
                fi
            fi

            # Do not add an algorithm to the config if common.sh is sourced
            # in the evaluation step. There another component for validation
            # will be invoked.
            if [ $SMLB_STEP != "validate" -o $key1 != $algorithm_const ]
            then
                add_conf "$key1.$key2 = $val"
            fi
        fi
    done
}


if [ "$#" -ne 1 ]; then
    echo usage: "$(basename "$0")" config.prop
    exit 1
fi

# export config options by: UPPERCASE, replace . with _, prefix with SMLB_
# keep this in sync with ProcessRunner#updateEnvironment
if [ -z "$SMLB_FILENAME_WORKDIR" ]; then
    eval "$(cat "$1" | grep -v 'settings.' | awk -F ' = ' '{gsub("[.]","_",$1); $1="SMLB_" toupper($1); printf "%s=\"%s\"\n",$1,$2}')"
    eval "$(cat "$1" | grep 'settings.' | sed 's/\"/\\"/g' | awk -F ' = ' '{gsub("[.]","_",$1); $1="SMLB_" $1; printf "%s=%s\n",$1,$2}')"
fi

# save parameters to variables
learning_task="$SMLB_LEARNINGTASK"
learning_problem="$SMLB_LEARNINGPROBLEM"
result_output_file="$SMLB_FILENAME_OUTPUT"

# define directory names
task_dir="$learning_task_dir_name"/"$learning_task"
data_dir="$learning_task_dir_name"/"$learning_task"/"$owl_dir_name"/"$data_dir_name"
task_config_file="$learning_task_dir_name"/"$learning_task"/"$owl_dir_name"/config.properties
lp_dir="$task_dir"/"$owl_dir_name"/"$lp_dir_name"/"$learning_problem"
# TODO: The Java app should load this
lp_config_file="$lp_dir"/dllearner.conf
echo $lp_config_file

dllearner_file="$(find_dllearner)"
if [ ! -x "$dllearner_file" ]; then
    echo DL-Learner not found. Please download it from http://dl-learner.org
    echo and unpack it into the learningsystems/dllearner directory.
    exit 1
fi

log Using DL-Learner "$dllearner_file"

if [ ! -d ../../"$task_dir" ]; then
    echo Learning task "$learning_task" not found.
    exit 1
fi

if [ ! -d ../../"$lp_dir" ]; then
    echo Learning problem "$learning_problem" not found in "$learning_task"
    exit 1
fi

# create working directory
tmpdir="$SMLB_FILENAME_WORKDIR"
conf="$tmpdir"/run.conf
#trap 'rm -rf "$TMPDIR"' 0

: > "$conf"

common_config() {
    prefix=
    prefix_sed_cmd=
    if [ -f ../../"$task_config_file" ]; then
	    prefix="$(grep '^prefix *[:=]' ../../"$task_config_file" | head -1 | tr = : | cut -f2- -d:)"
	    prefix_sed_cmd="-e s.^.ex:."
    fi
    if [ -f ../../"$lp_config_file" ]; then
        read_lp_conf ../../"$lp_config_file"
    fi

    if [ -n "$prefix" ]; then
	    add_conf 'prefixes = [ ("ex","'"$prefix"'") ]'
    fi


    # get all settings that were put under the 'settings' namespace
    for setting in $(set | grep 'SMLB_settings')
    do
        setting=$(echo "$setting" | sed 's/SMLB_settings_//g')
        wholekey=$(echo "$setting" | awk -F ' *= *' '{print $1}')
        cleankey=$(echo "$wholekey" | sed 's/_/./g')
        val=$(echo "$setting" | awk -F ' *= *' '{print $2}')

        add_conf "$cleankey=$(echo $val | sed "s/'//g")"

        # In case a component is defined in SMLB_settings_* a flag has
        # to be set.
        # A component is defined whenever a '.type' appears in the setting,
        # e.g. algorithm.type = "eltl". Then, it has to be checked which
        # kind of component was defined and the <component_type>_is_set
        # flag has to be updated accordingly. For all components with
        # <component_type>_is_set = 0 default components will be configured
        # further below.
        if [ $(echo "$cleankey" | grep -c "type") -ge 1 ]
        then
            if [ $(echo "$cleankey" | grep -c "$algorithm_const") -ge 1 ]
            then
                algorithm_is_set=1
            fi

            if [ $(echo "$cleankey" | grep -c "$reasoner_const") -ge 1 ]
            then
                reasoner_is_set=1
            fi
            
            if [ $(echo "$cleankey" | grep -c "$measure_const") -ge 1 ]
            then
                measure_is_set=1
            fi
    
            if [ $(echo "$cleankey" | grep -c "$lp_const") -ge 1 ]
            then
                lp_is_set=1
            fi
        fi
    done


    if [ $algorithm_is_set -eq 0 ]
    then
        add_conf "$algorithm_const.type" '=' '"celoe"'
        algorithm_is_set=0
    fi

    if [ $measure_is_set -eq 0 ]
    then
        add_conf "$measure_const.type" '=' '"gen_fmeasure"'
        measure_is_set=0
    fi
    
    add_conf
    sources=
    i=1
    for f in ../../"$data_dir"/*.owl; do
	    add_conf ks"$i".type '=' '"OWL File"'
	    add_conf ks"$i".fileName '=' '"'"$program_dir"/"$f"'"'
	    add_conf
	    sources="$sources${sources:+, }ks$i"
	    : $((i=i+1))
    done

    if [ $reasoner_is_set -eq 0 ]
    then
        add_conf "$reasoner_const.type" '=' '"closed world reasoner"'
        reasoner_is_set=0
    fi
    add_conf reasoner.sources '=' '{' "$sources" '}'
    add_conf

    if [ $lp_is_set -eq 0 ]
    then
        add_conf "$lp_const.type" '=' '"posNegStandard"'
        lp_is_set=0
    fi

    # load positive examples
    add_conf "$lp_const.positiveExamples" '=' '{'
    cat "$SMLB_FILENAME_POS" | remove_blanks | quote_examples >> "$conf"
    add_conf '}'
    add_conf

    # load negative examples
    add_conf "$lp_const.negativeExamples" '=' '{'
    cat "$SMLB_FILENAME_NEG" | remove_blanks | quote_examples >> "$conf"
    add_conf '}'
}
