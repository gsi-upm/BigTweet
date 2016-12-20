#do not add calls to functions at the beginnig or Rcaller from java will crash

loadDatasets<-function(){
obama <<- read.table("./configuration/obamaUsers.txt", header=TRUE, sep="\t")
palin <<- read.table("./configuration/palinUsers.txt", header=TRUE, sep="\t")
toyota <<- read.table("./configuration/toyotaUsers.txt", header=TRUE, sep="\t")
ford <<- read.table("./configuration/fordUsers.txt", header=TRUE, sep="\t")
sim<<- read.table("./output/statesPerAgentAndDay.txt", header=TRUE, sep="\t")
sim2<<- read.table("./output/statesPerAgentAndStep.txt", header=TRUE, sep="\t")
}

#compares two datasets of different lengths with users endorsing and denying per day, the intial position in dataset is considered position 0.
getLinearChartComparing<-function(d,dsim,title="Dataset vs simulated data"){
  xrange <- range(0:max(nrow(d),nrow(dsim))) 
  yrange <- range(c(d$endorses, d$denies,dsim$ENDORSER,dsim$DENIER)) #gets range (min and max of the vector obtained from joining the two "y" lines values)
  x1<- seq(from=0, nrow(d)-1) #d and dsim can have different  lengths, so there is a x for each dataset
  x2<- seq(from=0, nrow(dsim)-1)
  y <-list(d$endorses, d$denies, dsim$ENDORSER, dsim$DENIER)
  print(y)
  colors <- c("red","black","magenta", "darkgreen")
  linesNames<-c("Endorses","Denies","SimEndorses", "SimDenies")
  linetype <- seq(length(y)) #seq 1,2,3...
  plot(xrange, yrange, xlab="Day", ylab="Users percentage",type="n", main=title ) 
  #type=n is in plot to avoid showing ranges
  for(i in (1:length(y))) {  #change type if wanted  
    if(length(y[[i]])==length(x1)){ #d and dsim can have different  lengths
      x3=x1
    }
    else{
      x3=x2
    }
    lines(x3, y[[i]],col=colors[i],type="l",lwd=2, lty=linetype[i])
   
  }
  
  
  
  legend("bottomright",legend=linesNames,text.col=colors,col=colors, lty=linetype, cex=0.8,bty="n",lwd=2,inset=0.01)
}



#same than getLinearChartComparing, but loading datasets from table
getLinearChartComparingFromFile<-function(file1,file2){  
  aux1 <- read.table(file1, header=TRUE, sep="\t")
  aux2 <- read.table(file2, header=TRUE, sep="\t")
  getLinearChartComparing(aux1,aux2)
  
}


plotObama<-function(){
  loadDatasets()
  getLinearChartComparing(obama, sim)
}

plotPalin<-function(){
  loadDatasets()
  getLinearChartComparing(palin, sim)
 
}

plotToyota<-function(){
  loadDatasets()
  getLinearChartComparing(toyota, sim)
}

plotFord<-function(){
  loadDatasets()
  getLinearChartComparing(ford, sim)
}


#show chart with evolution in time of states
getLinearChartWithStates<-function(d,title="Users states in simulation"){
  xrange <- range(0:max(nrow(d))) 
  yrange <- range(c(d$ENDORSER,d$DENIER,d$INFECTED, d$VACCINATED, d$CURED, d$NEUTRAL)) #gets range (min and max of the vector obtained from joining the two "y" lines values)
  x<- seq(from=0, nrow(d)-1) 
  y <-list(d$ENDORSER,d$DENIER,d$INFECTED, d$VACCINATED, d$CURED, d$NEUTRAL)
  
  colors <- c("red","black","magenta", "darkgreen", "blue", "cyan")
  linesNames<-c("Endorsers","Deniers","Infected", "Vaccinated", "Cured", "Neutral")
  linetype <- seq(length(y)) #seq 1,2,3...
  plot(xrange, yrange, xlab="Day", ylab="Users percentage",type="n", main=title ) 
  #type=n is in plot to avoid showing ranges
  for(i in (1:length(y))) {  #change type if wanted      
    lines(x, y[[i]],col=colors[i],type="l",lwd=2, lty=linetype[i])
  }
  
  
  legend("topright",legend=linesNames,text.col=colors,col=colors, lty=linetype, cex=0.8,bty="n",lwd=2,inset=0.01)
}



#same than getLinearChartComparing, but loading datasets from table
getLinearChartChartWithStatesFromFile<-function(file1){  
  aux <- read.table(file1, header=TRUE, sep="\t")
  getLinearChartWithStates(aux)
}






#Pearson correlation between endorsers, deniers, and sum of them. D1 is the real dataset, d2 a greater simulated dataset
distance<-function(d,dsim){ 
  simEndorses<-dsim$ENDORSER[1:nrow(d)]
  simDenies<- dsim$DENIER[1:nrow(d)]
  if(nrow(dsim)<nrow(d)){#repeat last element if the sim does not have enough data
     simEndorses<- c(simEndorses, rep(simEndorses[nrow(dsim)], nrow(d)-nrow(dsim)))
     simEndorses<- simEndorses[!is.na(simEndorses)] #if sim has less rows, NA were added
     simDenies<- c(simDenies, rep(simDenies[nrow(dsim)], nrow(d)-nrow(dsim)))
     simDenies<- simDenies[!is.na(simDenies)]
  }
  endorses<-as.vector(dist(rbind(d[,1], simEndorses), method = "euclidian"))
  denies<- as.vector(dist(rbind(d[,2], simDenies), method = "euclidian"))
  return(endorses + denies)
  
}

distanceFromFiles<-function(file1,file2){ 
  aux1 <- read.table(file1, header=TRUE, sep="\t")
  aux2 <- read.table(file2, header=TRUE, sep="\t")
  return(distance(aux1,aux2))

}

#Pearson correlation between endorsers, deniers, and sum of them. D1 is the real dataset, d2 a greater simulated dataset
correlation<-function(d,dsim){  
  corr1<-cor.test(d[,1],dsim[1:nrow(d),1])$estimate
  corr2<-cor.test(d[,2],dsim[1:nrow(d),2])$estimate
  sum <-corr1+corr2
  return(as.vector(c(corr1,corr2,sum)))
  
}

correlationFromFiles<-function(file1,file2){
  aux1 <- read.table(file1, header=TRUE, sep="\t")
  aux2 <- read.table(file2, header=TRUE, sep="\t")
  return(correlation(aux1,aux2))
}

correlationPalin<-function(){
  loadDatasets()
  correlation(palin,sim)
}


correlationObama<-function(){
  loadDatasets()
  correlation(obama,sim)
}

correlationToyota<-function(){
  loadDatasets()
  correlation(toyota,sim)
}

correlationFord<-function(){
  loadDatasets()
  correlation(ford,sim)
}






#generate input for batch M2
#seq suggestions: (0.01,0.1,0.005), (0.001,0.025,0.0005), 
batchInputFileGeneratorModel2<-function(){
  library(jsonlite)
  library(plyr)
  users<-1000
  maxLinkPerNode<-10
  initiallyInfected<-2
  probInfect<-seq(0.01,0.1,0.005)
  probAcceptDeny<-seq(0.01,0.1,0.005)
  probMakeDenier<-seq(0.01,0.1,0.005)
  batchInput<- arrange(expand.grid(users=users,maxLinkPerNode=maxLinkPerNode, initiallyInfected=initiallyInfected,probInfect=probInfect,probAcceptDeny=probAcceptDeny,probMakeDenier=probMakeDenier),users)
  print(paste("Combinations of parameters: ", nrow(batchInput)))
  batchInput<-toJSON(batchInput, pretty=TRUE,digits=4)
  write(batchInput, file = "./configuration/BatchInputM2.json")
}

#generate input for batch M1
#seq suggestions: (0.01,0.1,0.005), (0.001,0.025,0.0005), 
batchInputFileGeneratorModel1<-function(){
  library(jsonlite)
  library(plyr)
  users<-1000
  maxLinkPerNode<-10
  initiallyInfected<-2
  probInfect<-seq(0.01,0.1,0.005)
  probAcceptDeny<-seq(0.01,0.1,0.005)
  timeLag<-seq(0,23,1)
  batchInput<- arrange(expand.grid(users=users,maxLinkPerNode=maxLinkPerNode, initiallyInfected=initiallyInfected,probInfect=probInfect,probAcceptDeny=probAcceptDeny,timeLag=timeLag),users)
  print(paste("Combinations of parameters: ", nrow(batchInput)))
  batchInput<-toJSON(batchInput, pretty=TRUE,digits=4)
  write(batchInput, file = "./configuration/BatchInputM1.json")
}

#generate input for studying beacons batch
#seq suggestions: (0.01,0.1,0.005), (0.001,0.025,0.0005), 
batchInputFileGeneratorStudyingBeaconsPalin<-function(){
  library(jsonlite)
  library(plyr)
  users<-1000
  maxLinkPerNode<-10
  initiallyInfected<-2
  probInfect<-0.02
  probAcceptDeny<-0.01
  probMakeDenier<-0.01
  beaconLinksNumber<- c(0, 5, 10, 25, 50, 75, 100, 200, 300, 400, 500,1000)
  beaconLinksCentrality<- c("b","c","d","r")
  seedNetwork <- 4 #best network for palin
  batchInput<- arrange(expand.grid(users=users,maxLinkPerNode=maxLinkPerNode, initiallyInfected=initiallyInfected,probInfect=probInfect,probAcceptDeny=probAcceptDeny,probMakeDenier=probMakeDenier,beaconLinksNumber=beaconLinksNumber,beaconLinksCentrality=beaconLinksCentrality, seedNetwork=seedNetwork),users)
  print(paste("Combinations of parameters: ", nrow(batchInput)))
  batchInput<-toJSON(batchInput, pretty=TRUE,digits=4)
  write(batchInput, file = "./configuration/BatchInputStudyingBeaconsPalin.json")
}

#show chart for studying beacons batch

plotBeaconStudy<-function(){
  library(jsonlite)
  d <- fromJSON("./output/BatchOutputForChart.json")
  xrange <- range(unique(d$links))
  yrange <- range(as.numeric(d$meanEndorsers))
  x<-  unique(d$links) 
  y <- list(subset(d, centrality=='b')$meanEndorsers, subset(d, centrality=='c')$meanEndorsers, subset(d, centrality=='d')$meanEndorsers, subset(d, centrality=='r')$meanEndorsers)
  colors <- c("red","black","magenta", "darkgreen")
  linesNames<-c("Betwenness","Closeness","Degrees", "Random")
  linetype <- seq(length(y)) #seq 1,2,3...
  #print(d)
  #print(x)
  #print(y)
  plot(xrange, yrange, xlab="Links", ylab="Mean of endorsers users",type="n", main="Use of beacons", xaxt = "n" ) 
  #type=n is in plot to avoid showing ranges
  
  for(i in (1:length(y))) {  #change type if wanted    
    lines(x, y[[i]],col=colors[i],type="l",lwd=2, lty=linetype[i])
  }
  
  axis(1, at =x) #only x values  axes
  legend("topright",legend=linesNames,text.col=colors,col=colors, lty=linetype, cex=0.8,bty="n",lwd=2,inset=0.01)
  
}


#getNMostImportantNodes, centrality can be "b" for betweeness, "c" for closeness, "d" for degree. 
#check the graph with   g<-read.graph("./output/lastgraph.xml", format="graphml")
getNMostImportantNodes<-function(file, centrality, returnedNodes){
  library(igraph)
  g<-read.graph(file, format="graphml")
  
  if(centrality=="b"){
    clist  <-betweenness(g) #list of betweenness, position x, bet of node x (id is x+1)  
  }
  
  
  if(centrality=="d"){
    clist  <-degree(g) #list of betweenness, position x, bet of node x (id is x+1)  
  }
  
  if(centrality=="c"){
    clist  <-closeness(g) #list of betweenness, position x, bet of node x (id is x+1)  
  }
  
  if(centrality=="r"){
    return(sample(V(g)$id, returnedNodes)) 
  }
  
  idsOrdered<-order(clist, decreasing = TRUE) #centrality, ordered
  ids<- idsOrdered[1:returnedNodes]
  V(g)[ids]$id
}
