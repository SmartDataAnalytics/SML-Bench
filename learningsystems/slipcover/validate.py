#!/usr/bin/env python

import argparse

try:
    import configparser
except ImportError:
    import ConfigParser as configparser

import logging
import os
import sys
import stat
import subprocess
# import platform
import atexit
import signal
import ctypes
import shlex

logging.basicConfig(level=logging.DEBUG, format='%(asctime)s %(message)s')
_log = logging.getLogger()

learning_task_dir_name = 'learningtasks'
prolog_dir_name = 'prolog'
data_dir_name = 'data'
tool_specific_data_dir = 'slipcover'
lp_dir_name = 'lp'
swipl_executable_name = 'swipl'
slipcover_script_name = 'slipcover.pl'
extract_inter_script = "extract_facts.pl"

class NoSwiplInstallationFoundException(Exception):
    """Raised when no installed SWI-Prolog could be found."""

class NoSLIPCOVERScriptFound(Exception):
    """Raised when no SLIPCOVER script could be found"""
    
def copy_files_around(task_id, lp_id, target_dir, file_name_base, file_pos_path,
                      file_neg_path, file_knowledge_path, prolog):
    """
    """
    extract_inter_script_path = os.path.join(os.getcwd(),extract_inter_script)
    
    # copy the file that will be taken as input by SLIPCOVER
    out = open(os.path.join(target_dir, file_name_base + '.pl'), 'w')
    subprocess.call(['cat', file_knowledge_path], cwd=target_dir, stdout=out)
    # convert the positive examples
    file_pos_converted_path = os.path.join(target_dir, "file_pos_converted.pl")
    
    command = prolog + " --quiet -l " + extract_inter_script_path + " -g "

    args = shlex.split(command)
    args.append("extract_pos_examples(\'%s\',\'%s\'),halt." % (file_pos_path, file_pos_converted_path))
    
    subprocess.call(args, cwd=os.getcwd())
    # copy the converted negative examples
    subprocess.call(['cat', file_pos_converted_path], cwd=target_dir, stdout=out)
    # remove file containing the converted positive examples
    os.remove(file_pos_converted_path)
    
    
    # convert the negative examples
    file_neg_converted_path = os.path.join(target_dir, "file_neg_converted.pl")
    
    command = prolog + " --quiet -l " + extract_inter_script_path + " -g "

    args = shlex.split(command)
    args.append("extract_neg_examples(\'%s\',\'%s\'),halt." % (file_neg_path, file_neg_converted_path))
    
    subprocess.call(args, cwd=os.getcwd())
    # copy converted negative examples
    subprocess.call(['cat', file_neg_converted_path], cwd=target_dir, stdout=out)
    # remove file containing the converted negative examples
    os.remove(file_neg_converted_path)
    # all examples in one fold
    out.write("fold(all,F) :- findall(I,int(I),F).")
    out.close()
    
    return os.path.join(target_dir, file_name_base + ".pl")


def validate(swipl_executable, learned_clauses, target_dir, knowledge_file_path):
    command = swipl_executable + " --quiet -l " +  + " -g "
    args = shlex.split(command)
    args.append("consult(%s),test(),halt." % (knowledge_file_path,))
    
    subprocess.call(args, cwd=tool_specific_dir, stdout=out)
    cmd = ""
    
    
def find_swipl():
    swipl_exec_path = subprocess.check_output(['which', swipl_executable_name])
    swipl_exec_path = swipl_exec_path.strip()

    if swipl_exec_path == '':
        msg = 'No SWI-Prolog installation could be found. Please make sure you ' \
                  'have SWI-Prolog installed via your package manager or download ' \
                  'and manually install it ' \
                  'from http://www.swi-prolog.org/Download.html'
        raise NoSwiplInstallationFoundException(msg)

    return swipl_exec_path

# It finds where is the script 'slipcover.pl' 
def find_slipcover_script():
    if os.path.isfile(os.path.join(os.getcwd(), slipcover_script_name)):
        # SLIPCOVER executable resides in the same directory
        return os.path.join(os.getcwd(), slipcover_script_name)

    else:
        try:
            slipcover_script_path = subprocess.check_output(
                ['locate', slipcover_script_name])
        except subprocess.CalledProcessError:
            slipcover_script_path = ''

        if slipcover_script_path == '':
            msg = 'No %s script could be found. Please download it ' \
                  'from https://sites.google.com/a/unife.it/ml/slipcover ' \
                  'or copy it from ~/lib/swipl/pack/cplint/prolog after ' \
                  'you have installed the cplint pack ' \
                  'and put it into the learning ' \
                  'system\'s directory' % (slipcover_script_name)
            raise NoSLIPCOVERScriptFound(msg)

        return slipcover_script_path

def read_config(path):
    conf = configparser.ConfigParser()
    conf.read(path)

    settings = {}
    for item in conf.items('main'):
        setting, raw_value = item
        settings[setting] = raw_value

    for item in conf.items('filename'):
        setting, raw_value = item
        settings['filename.' + setting] = raw_value

    for item in conf.items('data'):
        setting, raw_value = item
        settings['data.' + setting] = raw_value
    if conf.has_section('preprocessing'):
        for item in conf.items('preprocessing'):
            setting, raw_value = item
            settings['preprocessing.'+setting] = raw_value

    return settings


if __name__ == '__main__':
    argparser = argparse.ArgumentParser()
    argparser.add_argument('config_file')
    args = argparser.parse_args()
    # read the configuration file
    cfg = read_config(args.config_file)
    
    learning_task_id = cfg['learningtask']
    learning_problem_id = cfg['learningproblem']
    input_file = cfg['input']
    output_file_path = cfg['output']
    # read the learned theory 
    learned_clauses = []
    with open(input_file) as res:
        for line in res.readlines():
            learned_clauses.append(line.strip())
            
    file_name_base = learning_task_id + '_' + learning_problem_id
    target_dir = cfg['data.workdir']
    _log.debug('Target dir is %s' % target_dir)
    swipl_executable = find_swipl()
    slipcover_script_path = find_slipcover_script()
    # copy input files for execution
    knowledge_file_path = copy_files_around(file_name_base, cfg)
    # validation execution!
    nPos, nNeg, results = validate(learned_clauses, target_dir, knowledge_file_path)
    # write the output configuration file
    write_config(nPos, nNeg, results, output_file_path)
    
    _log.debug('SLIPCOVER run finished.')
    _log.debug('Results written to %s' % output_file_path)
