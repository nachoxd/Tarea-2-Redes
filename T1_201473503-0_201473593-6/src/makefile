JCC = javac
DR= com/
NAME1= Client.jar
NAME2= Server.jar
JFLAGS = -g
LIB = -classpath lib/json-simple-1.1.1.jar


default: CLASES1 jar CLASES2 jar2


CLASES1: Client.class

CLASES2: Server.class 

Client.class: $(DR)Client.java
	$(JCC) $(JFLAGS) $(LIB) $(DR)Client.java $(LIB)


Server.class: $(DR)Server.java
	$(JCC) $(JFLAGS) $(LIB) $(DR)Server.java $(DR)ClientHandler.java




jar:
	jar -cvfm $(NAME1) MANIFEST1.MF $(DR)*.class

jar2:
	jar -cvfm $(NAME2) MANIFEST2.MF $(DR)*.class

run:
	echo mueva los jar a la carpeta deseada y ejecute


clean:
	$(RM) $(DR)*.class
	$(RM) $(NAME1)
	$(RM) $(NAME2)