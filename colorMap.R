library(ggplot2)
library(wesanderson)
library(ggpubr)
library(dplyr)
library(readr)

n = 186

dist <- read_csv("Desktop/NLPfinal/dist.tsv")

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
  labs(x="",y="",fill="")

dist[c(80:83),c(80:83)]
