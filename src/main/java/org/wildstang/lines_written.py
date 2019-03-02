#!/usr/bin/env python3

import subprocess
import re
from collections import defaultdict
import pprint


def call(cmd):
    proc = subprocess.run(cmd, stdout=subprocess.PIPE)
    proc.check_returncode()
    return proc.stdout.decode()


rev_text = call(['git', 'rev-list', '--all', '--pretty=format:%H %ae'])
authors = dict()

for line in rev_text.splitlines():
    if line[:6] == "commit":
        continue

    commit, email = line.split()
    print(commit, email)
    authors[commit] = email
    
log_text = call(['git', 'log', '--shortstat', '--pretty=oneline'])
insertions = dict()
current_commit = None

for line in log_text.splitlines():
    if line[0] != " ":
        commit = line[:40]
    else:
        match = re.search(r'(\d+) insertions', line)
        if match:
            insertions[commit] = int(match.group(1))
            print(commit, authors[commit], insertions[commit])

print(len(insertions))

assert set(authors.keys()) >= set(insertions.keys())

insertions_by_author = defaultdict(lambda: 0)

for commit in insertions.keys():
    insertions_by_author[authors[commit]] += insertions[commit]

pprint.pprint(insertions_by_author)
print(sum(insertions_by_author.values()))

instructors = {'jaykinzie@gmail.com',
               '42500981+smccrorie@users.noreply.github.com',
               'zjmccord@gmail.com',
               'Zack@10.0.0.166',
               'slider6791@gmail.com'}

student_insertions = {author: insertions_by_author[author]
    for author in insertions_by_author.keys() if author not in instructors}

pprint.pprint(student_insertions)

print(sum(student_insertions.values()))



