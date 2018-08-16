# Terminology

## Node or Peer

A Decent Messaging `node` or `peer` is any running instance of the Decent Messaging Java application. Multiple nodes interconnect to form the encrypted Decent Messaging peer-to-peer network.

## Decent Messaging (DM) Addresses

A `DM Address` is the equivalent of an email address in the DecentMessaging system. It a large string of letters, numbers and symbol. The format of a DM address is:

1. A DM Address version string
2. A comma character (`,`)
3. The recipient's public key modulus (base64 encoded)
4. A comma character (`,`)
5. The recipient's public key exponent (base64 encoded)

DM addresses are therefore very large. An example of a typical DM address is:

```
A,rxR0cJJ79NOa+OjhZjKGDUpk+uCBEAEJcnxmu/z7CB/3L+a4/vUAmzD4r8W0QJLM7vjUlymRoD3wLxnzWLcM6AhApFaz+o4OqXe8AB3gaC8fs9VetJtZL/LmT5lVwvB6acyyLOt1wYPOQ1kcx6bCSV4B4nFohAsuq99LljY33J3xKRcUFlBEhqILt91Q4uBzP3m0anmy2I0N8yn6oO5yHcGFmFL0nKgMDm93dhPDzIobd5Ct4suc7GwCfmej/vNoLMz5vrjozXY8ppJyHV4o6fJBdsBUZ7S/gMtvVhFk5OsL1jTEOsH5o2GdbwDXDtTxFRy26kquzCf2xeoinrTNPsMXZs2q9Yg/kCMR9Z+z8NKDfJLz5pUeEb93ICW8E15KZWUWuEplPoUb6uc3OznpdjZHHj8dBI2jmZFoxactHjR7AMK/Ct+qNXWLxlhS3FLwwxa0eYIlJACKURBTU7AVSEeCdynnz8VI3ebR9DLVxnaXdusg4DiSaL3keP/w76GsiCJaI+RvBBg8+ax/9i1PI2DmOWOb7bEGE4o2XvmWtU1Y8toCV9neTSuu8iPpWCI/YCrXSNqV+et5BSdfG/NNbG/hwcmRVpOIPt+Zz2kWiw71ZegOvSXUC2gJy2/ohTBTGVa22jWZYTxu/i7jrrwyF+47cxZyUZJvR5H3g6UoJDc=,AQAB
```

## Message

A `message` is the main form of communication between nodes. A message's payload is an encrypted binary blob. Decrypted, this blob contains details on the sender, subject and main body content of the message.

Messages are encrypted with the public key of the intended recipient (derived from the DM address). This means only the intended recipient can correctly decrypt and read the message content. A decrypted message is then considered to be a personal message by the recipient node. 

## Personal Messages

A `personal` message is a message that was intended for the currently run DecentMessaging node, and thus has been successfully decrypted and stored.
