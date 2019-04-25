import requests
from bs4 import BeautifulSoup
import re

def cleanhtml(raw_html):
  cleanr = re.compile('<.*?>')
  cleantext = re.sub(cleanr, '', raw_html)
  cleantext = re.sub('[\r\n\t]', '', cleantext)
  cleantext = re.sub('^\s+', '', cleantext)
  return cleantext

page = requests.get("https://www.springfieldspringfield.co.uk/view_episode_scripts.php?tv-show=the-office-us&episode=s01e01")
page.status_code

soup = BeautifulSoup(page.content, 'html.parser')

num_eps = [6,22,24,14,26,25,25,24,23]

for season in range(1,10):
    for ep in range(1,num_eps[season-1]+1):
        if ep < 10:
            ep_str = "s0" + str(season) + "e0" + str(ep)
        else:
            ep_str = "s0" + str(season) + "e" + str(ep)
        page = requests.get("https://www.springfieldspringfield.co.uk/view_episode_scripts.php?tv-show=the-office-us&episode=" + ep_str)
        soup = BeautifulSoup(page.content, 'html.parser')
        with open(ep_str, "w+") as file:
            file.write(cleanhtml(str(soup.find('div', class_='scrolling-script-container'))))


# BOW

num_eps = [6,22,24,14,26,25,25,24,23]
episodes = []
for season in range(1,10):
    for ep in range(1,num_eps[season-1]+1):
        if ep < 10:
            ep_str = "s0" + str(season) + "e0" + str(ep)
        else:
            ep_str = "s0" + str(season) + "e" + str(ep)
        with open(ep_str, "r") as file:
            episodes.append(file.readline())

from sklearn.feature_extraction.text import CountVectorizer
import pandas as pd

vectorizer = CountVectorizer()
X = vectorizer.fit_transform(episodes)

df = pd.DataFrame(X.toarray())


vectorizer

for i in vectorizer.get_feature_names():
    if 'does' in i:
        print(i)

df.shape

df = df.div(df.sum(axis=1), axis=0)

df.to_csv("bow.tsv", sep='\t', header=False, index=False)


