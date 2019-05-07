library(ggplot2)
library(wesanderson)
library(ggpubr)
library(dplyr)
library(readr)

episode_ratings <- read_csv("Desktop/NLPfinal/episode_ratings.csv")

episode_ratings <- episode_ratings %>% 
  mutate(season = substr(episode,3,3)) #%>% 
  #filter(perplexity > 75)

ggplot(episode_ratings, aes(x=perplexity, y=rating, col=season)) + geom_path()


sppls = c(148.10, 150.49, 161.08, 161.20, 171.72, 156.86, 140.83, 155.31, 154.17)
srates = (episode_ratings %>% group_by(season) %>% summarize(rate = mean(rating)))$rate
season = c("1","2","3","4","5","6","7","8","9")
season_ratings <- data.frame(sppls, srates, season)

ggplot(season_ratings, aes(x=sppls, y=srates)) + geom_path() + geom_point(aes(col=season))

