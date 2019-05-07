library(ggplot2)
library(wesanderson)
library(ggpubr)
library(dplyr)
library(readr)

dist <- read_csv("Desktop/NLPfinal/idfdist.tsv") # change to file you want
n <- nrow(dist)

x <- rep(c(1:n),n)
y <- c()
for (i in c(1:n)) {
  y <- c(y, rep(i,n))
}
vals <- c(rep(0,n^2))
k <- 1
for (row in dist) {
  for (j in c(1:n)) {
    vals[k] <- row[j]
    k <- k + 1
  }
}

pal <- wes_palette(3, name="Zissou1", type="continuous") 
val.df <- data.frame(x=x,y=y,c=vals)
ggplot(val.df, aes(x=x,y=y)) + 
  geom_raster(aes(fill = c)) + 
  scale_fill_gradientn(colours=pal) + 
  labs(x="",y="",fill="") # + # for seasons  
  #%scale_x_discrete(name ="Season",limits=c(1:9)) + 
  #scale_y_discrete(name ="Season",limits=c(1:9))


# write the average distances to a file
# write.table(sort(colSums(dist)), "ep_dists")


episode_ratings <- read_csv("Desktop/NLPfinal/episode_ratings.csv") %>% 
  mutate(dists = colSums(dist)) %>%
  mutate(season = substr(episode,3,3))

ggplot(episode_ratings, aes(x=perplexity, y=dists)) + geom_point(aes(col=season))
sppls = c(148.10, 150.49, 161.08, 161.20, 171.72, 156.86, 140.83, 155.31, 154.17)
sdists = (episode_ratings %>% group_by(season) %>% summarize(dsts = mean(dists)))$dsts
season = c("1","2","3","4","5","6","7","8","9")
season_ratings <- data.frame(sppls, sdists, season)

ggplot(season_ratings, aes(x=sppls, y=sdists)) + geom_point(aes(col=season))
