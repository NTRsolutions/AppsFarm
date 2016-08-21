AppsFarm
==============================================================================================
Author: Mariusz Jacyno, Jakub Homlala, Dawid Pohling
Technologies: EAR

Run the Arquillian Tests 
-------------------------
To run tests execute this maven build goal: `package wildfly:undeploy` and then execute this maven build goal: `clean test -Parq-wildfly-remote`. Remember to start wildfly server before tests.

Example test should be in this format:
```java
@RunWith(Arquillian.class)
public class TestClassName {

	@Deployment
	public static WebArchive createDeployment() {
		File[] files = Maven.resolver().loadPomFromFile("pom.xml").importRuntimeDependencies().resolve()
				.withTransitivity().asFile();
		return ShrinkWrap.create(WebArchive.class, "test.war").addPackages(true, "web").addPackages(true, "ejb")
				.addAsLibraries(files).addAsWebInfResource(EmptyAsset.INSTANCE, 		ArchivePaths.create("beans.xml"))
				.addAsResource("META-INF/persistence.xml", "META-INF/persistence.xml");

	}
	
	@Test
	public void test_something(){
	
	}
	
	
}
```

Configure Wildfly to run the app 
---------------------

1.	In mysql create database:
afm
2.	In mysql make sure that user root (with password Treasur3) has privileges for afm database 
3.	In wildfly add datasource section to standalone-full.xml file (under datasource sections). 

<datasource jndi-name="java:jboss/datasources/afmDS" pool-name="intellisoft-afm" enabled="true" use-java-context="true">
                    <connection-url>jdbc:mysql://localhost:3306/afm</connection-url>
                    <driver>mysql</driver>
                    <pool>
                        <min-pool-size>50</min-pool-size>
                        <max-pool-size>200</max-pool-size>
                        <prefill>true</prefill>
                        <flush-strategy>EntirePool</flush-strategy>
                    </pool>
                    <security>
                        <user-name>root</user-name>
                        <password>Treasur3</password>
                    </security>
                    <timeout>
                        <blocking-timeout-millis>30000</blocking-timeout-millis>
                        <idle-timeout-minutes>5</idle-timeout-minutes>
                    </timeout>
                    <statement>
                        <prepared-statement-cache-size>32</prepared-statement-cache-size>
                        <share-prepared-statements>true</share-prepared-statements>
                    </statement>
                </datasource>

4.	Run wildfly using standalone-full.xml configuration file 



 
Configure Maven to build and deploy app
---------------


1. In Eclipse select Run->Run configurations
2. On lef-hand menu select Maven Build and on top left menu click + to create new launch configuration (name it deploy)
3. In base directory field define: ${workspace_loc:/afm-ear}
4. In goals field define: clean package wildfly:deploy
5. Click Apply and Run (project should build and deploy on the app server)


Configure Maven to undeploy app
---------------

1. In Eclipse select Run->Run configurations
2. On lef-hand menu select Maven Build and on top left menu click + to create new launch configuration (name it undeploy)
3. In base directory field define: ${workspace_loc:/afm-ear}
4. In goals field define: clean package wildfly:undeploy
5. Click Apply and Run (project should build and undeploy on the app server)

Access the application 
---------------------

The application will be running at the following URL: <http://localhost:8080/afm>.

Pulling existing ear repo from github onto local eclipse (when we want to work on this project using github repo)
---------------------


1. Tut: https://alextheedom.wordpress.com/cloud/java-ee-project-from-remote-git-repository/
2. in git view select clone repository
3. select repo from uri
4. provide git https url for the github project
5. https://github.com/aelbereth/Afm.git
6. select local folder where to download it
7. once repo is cloned select import existing maven project and provide url

Creating and publishing new ear repo to github (only when we want to create new github repo from existing java code)
---------------------
1. create github repo (with no txt file) on github website
2. team->share and create repo in parent folder for each ear module (this will bind them so that the top module is the global one to push to github)
3. team->add to index (add to index on global ear module)
4. commit locally
5. push to github (use https with user credentials for setting up github connection)
(select master branch not head in push dialog in egit)
6. http://stackoverflow.com/questions/10365958/when-pushing-to-remote-git-repo-using-egit-in-eclipse-what-should-i-choose

Tut: http://teddsprogrammingblog.blogspot.com/2012/02/git-eclipse-and-maven-multi-module.html


Github tuts
---------------------

1. http://www.vogella.com/tutorials/EclipseGit/article.html
2. http://git-scm.com/book/en/v1/Git-Branching-What-a-Branch-Is 
3. http://git-scm.com/book/en/v1/Git-Branching-Basic-Branching-and-Merging (branch merging)
4. http://git-scm.com/book/en/v1/Git-Branching-Branching-Workflows
5. http://stackoverflow.com/questions/2046752/revision-control-system-with-multiple-similar-projects-one-customized-for-each (good tut how to handle multiple yet similar versions)
6. http://stackoverflow.com/questions/2046752/revision-control-system-with-multiple-similar-projects-one-customized-for-each (multiple similar projects)
7. https://mackuba.eu/2010/02/04/sharing-code-between-projects-with-git-subtree/ (subtrees)
8. http://www.avajava.com/tutorials/lessons/how-do-i-use-the-team-synchronizing-perspective-in-eclipse.html?page=1 (synchronize perspective)
9. http://help.eclipse.org/luna/index.jsp?topic=%2Forg.eclipse.platform.doc.user%2Ftasks%2Ftasks-115.htm (synchronize perspective)
10. https://wiki.eclipse.org/EGit/User_Guide#Git_Staging_View (git staging)
