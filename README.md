# RBUDP vs. TCP File Transfer Protocol Comparison

This project demonstrates and compares **Reliable Blast User Datagram Protocol (RBUDP)** and **TCP** for file transfer across a local network. By implementing both protocols in custom sender and receiver applications with simple GUIs, this project enables users to analyze protocol performance under varying conditions.

## Project Overview

### Features

#### Sender Application
- **GUI Interface**: User-friendly interface for selecting and transferring files.
- **File Transfer Options**: Supports file transfer over both TCP and RBUDP.
- **RBUDP Implementation**:
  - Uses UDP for data transfer and TCP for signaling.
  - Unique sequence numbers in datagram packets with support for wrap-around.

#### Receiver Application
- **GUI Interface**: Displays progress of incoming file transfers.
- **Error Handling**: Manages dropped packets and reorders out-of-sequence RBUDP datagram packets.

