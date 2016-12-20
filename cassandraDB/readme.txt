
	The python script "getbigtweetdb.py"can be used to generate a File, "output.txt", useful as comparison with simulated values in the tool BigTweet.
Before running the script, it's necessary to run a docker with cassandra and that cassandra contains the dataset. For example with the command:

docker run --name cassandra -v /BigTweet/CassandraDB/cassandra:/var/lib/cassandra -d cassandra

In the case above, we use the directory "cassandra" in this same directory. 
Read the comments in the script to know what values to change for your case.


Where get a docker with cassandra?, suggestion:
https://github.com/docker-library/docs/tree/master/cassandra