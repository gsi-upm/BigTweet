from cassandra.cluster import Cluster

# Default IP in cassandra docker. You must verify that yours is the same.
IPCassandra = '172.17.0.2'

# Posibles word, hashtag or anything else to filter
hashtag11 = 'Toyota'
hashtag12 = 'toyota'

cluster = Cluster([IPCassandra])
session = cluster.connect()

session.execute('USE test_final')
rowsClas = session.execute('select * from tweets_clasificados')

# Get rows about our marketing campaign from the db tables using the hashtag
rowsDonettingClas = []
for row in rowsClas:
	content = row.tweet_content
	if ((hashtag11 in content) or (hashtag12 in content)):
		rowsDonettingClas.append(row)

# Clasification by days. 
# Set the years maximum and minimum for your case
rowsByDate = []
from_year = 2009
to_year = 2013
for year in range(from_year, to_year):
	for month in range(0,13):
		for day in range(0,32):
			rowsByDay = []
			date = str(year) + '-'
			if month < 10:
				date = date + '0'+ str(month) + '-'
			else:
				date = date + str(month) + '-'
			if day < 10:
				date = date + '0'+ str(day)
			else:
				date = date + str(day)
			for row in rowsDonettingClas:
				dateRow = str(row.date)
				if date in dateRow:
					rowsByDay.append(row)
			rowsByDate.append(rowsByDay)

#Clasification by user according to their attitude for each day
deniesByDay = []
endorsesByDay = []
neutralByDay = []
pos_aux = 0
total_users = []
for rowsDay in rowsByDate:
	denies = 0.0
	endorses = 0.0
	neutral = 0.0
	users_by_day = []
	for row in rowsDay:
		if row.user not in total_users:
			total_users.append(row.user)
		if row.user not in users_by_day:
			users_by_day.append(row.user)
			tweet_sentiment = row.tweet_sentiment
			# The strings "NEUTRAL", "POSITIVE" and "NEGATIVE" must be change by your possibles values 
			# in the column with the attitude clasification.
			if str(tweet_sentiment) == 'NEUTRAL':
				neutral = neutral + 1
			elif str(tweet_sentiment) == 'POSITIVE':
				endorses = endorses + 1
			elif str(tweet_sentiment) == 'NEGATIVE':
				denies = denies + 1
		else:
			pass
	if pos_aux != 0:
		neutral = neutral + neutralByDay[pos_aux-1]
		endorses = endorses + endorsesByDay[pos_aux-1]
		denies = denies + deniesByDay[pos_aux-1]
	deniesByDay.append(denies)
	endorsesByDay.append(endorses)
	neutralByDay.append(neutral)
	pos_aux = pos_aux + 1

deniesByDayPer = []
endorsesByDayPer = [] 
neutralByDayPer = []

pos_aux2 = 0
total = len(total_users)

for n in deniesByDay:
	deniesByDayPer.append((deniesByDay[pos_aux2]/total)*100)
	neutralByDayPer.append((neutralByDay[pos_aux2]/total)*100)
	endorsesByDayPer.append((endorsesByDay[pos_aux2]/total)*100)
	pos_aux2 = pos_aux2 + 1

#Generating the output in .txt file with the proper structure to can be used as comparing file in BigTweet
file = open('output.txt','w+')
file.write('\"endorses\"\t'+'\"denies\"'+'\n')
pos_aux3 = 0
file.write(str(endorsesByDayPer[pos_aux3])+'\t'+str(deniesByDayPer[pos_aux3])+'\n')
pos_aux3 = 1
for n in range(0,len(deniesByDayPer)-1):
	if(endorsesByDayPer[pos_aux3] != endorsesByDayPer[pos_aux3-1] or deniesByDayPer[pos_aux3] != deniesByDayPer[pos_aux3-1]):
		file.write(str(endorsesByDayPer[pos_aux3])+'\t'+str(deniesByDayPer[pos_aux3])+'\n')
	pos_aux3 = pos_aux3 + 1
file.close()
