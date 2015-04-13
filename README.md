# oraclecoherence
This requires basic understanding for Oracle Coherence, or one can read how oracle coherence extend proxies / client & server works.

Ref:http://docs.oracle.com/cd/E15357_01/coh.360/e15726/gs_example.htm#COHCG4921

Oralce Coherence Extend Client : Coherence*Extend is a protocol which is used for non-members of cluster to get access to Coherence services. Extend is using TCP connection to one of cluster members (which should host proxy service) and use this member as a relay. Normally client process is creating single TCP connection


