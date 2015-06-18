Camel AMQP/ActiveMQ Project
======================

The goal is to consume messages from Twitter as well as other social feeds and have them delivered to an AMQP destination for delivery to a FeedHenry demo.  To get this to work you must follow the steps below to install the application as there are missing packages and apis in the bundles availabe for AMQP:

## Install
First download and install JBoss AMQ or JBoss Fuse 6.2 build 128. Now open up the activemq.xml file under the <installation location>/etc and add the AMQP transport:

    <transportConnector
        name="amqp"
        uri="amqp://0.0.0.0:5672" />

Start the container
    ./bin/amq clean

Now install the bundles at the command line interface:

First install the base qpid API

    jboss@fuse:>install -s mvn:org.apache.servicemix.bundles/org.apache.servicemix.bundles.qpid/0.32_1

Now install the qpid 1_0 amqp jms apis.  These are not availabe as OSGi bundles so they must be wrapped using the following syntax:

    jboss@fuse:>install -s wrap:mvn:org.apache.qpid/qpid-amqp-1-0-common/0.32\$Bundle-Version=0.32&Bundle-Name=amqp-1-0-common-0.32&Bundle-SymbolicName=org.apache.qpid.common&Export-Package=org.apache.qpid.amqp_1_0*\;version=0.32\;-noimport:=true
    jboss@fuse:>install -s wrap:mvn:org.apache.qpid/qpid-amqp-1-0-client/0.32\$Bundle-Version=0.32&Bundle-Name=amqp-1-0-client-0.32&Bundle-SymbolicName=org.apache.qpid.client&Export-Package=org.apache.qpid.amqp_1_0*\;version=0.32\;-noimport:=true
    jboss@fuse:>install -s wrap:mvn:org.apache.qpid/qpid-amqp-1-0-client-jms/0.32\$Bundle-Version=0.32&Bundle-Name=amqp-1-0-client-jms-0.32&Bundle-SymbolicName=org.apache.qpid.client.jms&overwrite=merge&Export-Package=org.apache.qpid.amqp_1_0.jms*\;version=0.28\;-noimport:=true

Install the Camel features:

    jboss@fuse:>features:install camel-twitter
    jboss@fuse:>features:install camel-jackson
    jboss@fuse:>features:install camel-jms
    jboss@fuse:>features:install camel-amqp

Finally build and install the Demo:

    mvn clean install

    jboss@fuse:>install -s mvn:org.jboss.fuse.demo/org.jboss.fuse.demo.feedhenry/1.0.0-SNAPSHOT

The messages should appear on the org.jboss.fuse.demo.feedhenry queue in AMQ.

