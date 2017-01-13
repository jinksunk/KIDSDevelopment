This folder contains a small sample evaluation dataset from public domain sources. It includes the following components:

* packets.pcap - A set of packets in libpcap format (ie. as captured by the tcpdump program). Taken from DARPA 1999, Training
                 set week 1, Monday, first 10k IP packets from outside.tcpdump.
                 The timeframe covered is: 920293206.794673 - 920294405.456286, or approximately 20 minutes
* flows.txt    - A set of network flow records corresponding with the packets; produced using yaf tools:
                # yaf --in packets.pcap --out packets.ipfix
		# yafscii --in packets.ipfix --out flows.txt --tabular
* loglines.txt - Raw event log lines 
* testabox.owl - A test ABOX file describing the experimental setup
* packetlabels.txt - A set of labels for the packet data, labeling event EV1
* flowlabels.txt - A set of labels for the flow data, labeling event EV1
* loglabels.txt - A set of labels for the log data, labeling event EV1
* experiment.prop - A properties file to run KIDS on this experimental data

The abox file should be completed with local detector descriptions using the GUI interface. Once done, the following is possible:

* Run a KIDS Assessment to generate / measure detection capabilities;
* Train a set of detectors and run them in streaming mode (requires additional ABOX setup for streaming interfaces)

== Data Preparation ==
The data was prepared using the following steps:
1) Generate the PCAP file: tcpdump -r $DATA/DARPA-1999/Training/w1/Thursday/outside.tcpdump -c 100 -w ./smallStartSample.pcap ip
3) Generate loglines from the PCAP file:
   * Every telnet 3-way handshake produces a log entry
   * Every e-mail session produces a log entry
4) Generate attack data / labels according to the following signals:
5) Generate the netflow data from the PCAP file:
6) Review combined data files and label files
7) Generate the properties file using the GUI tool
*) Complete the ABOX for detectors using the GUI
