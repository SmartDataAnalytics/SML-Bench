#!/bin/sh
tool_name=dllearner

learning_task_dir_name=learningtasks
owl_dir_name=owl
data_dir_name=data
lp_dir_name=lp
dllearner_executable_name=cli

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


if [ "$#" -ne 1 ]; then
    echo usage: "$(basename "$0")" config.prop
    exit 1
fi

if [ -z "$SMLB_DATA_WORKDIR" ]; then
    eval "$(cat "$1" | awk -F ' = ' '{gsub("[.]","_",$1); $1="SMLB_" toupper($1); printf "%s=\"%s\"\n",$1,$2}')"
fi

# save parameters to variables
learning_task="$SMLB_LEARNINGTASK"
learning_problem="$SMLB_LEARNINGPROBLEM"
result_output_file="$SMLB_OUTPUT"

# define directory names
task_dir="$learning_task_dir_name"/"$learning_task"
data_dir="$learning_task_dir_name"/"$learning_task"/"$owl_dir_name"/"$data_dir_name"
task_config_file="$learning_task_dir_name"/"$learning_task"/"$owl_dir_name"/config.properties
lp_dir="$task_dir"/"$owl_dir_name"/"$lp_dir_name"/"$learning_problem"

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
tmpdir="$SMLB_DATA_WORKDIR"
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
    if [ -n "$prefix" ]; then
	add_conf 'prefixes = [ ("ex","'"$prefix"'") ]'
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

    add_conf reasoner.type '=' '"closed world reasoner"'
    add_conf reasoner.sources '=' '{' "$sources" '}'
    add_conf
    add_conf lp.type '=' '"posNegStandard"'

    # load positive examples
    add_conf lp.positiveExamples '=' '{'
    cat "$SMLB_FILENAME_POS" | remove_blanks | quote_examples >> "$conf"
    add_conf '}'
    add_conf

    # load negative examples
    add_conf lp.negativeExamples '=' '{'
    cat "$SMLB_FILENAME_NEG" | remove_blanks | quote_examples >> "$conf"
    add_conf '}'
}
