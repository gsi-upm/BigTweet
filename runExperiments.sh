rm nohup.out
rm  ./output/BatchOutput.json
ant clean
nohup ant run &
