# Ride-Sharing Backend System

This project is a simple backend for a ride-sharing app (like Uber or Bolt), built in Java.  
It manages drivers, passengers, rides, and automatic ride expiry — all in memory (no database needed).

---

## Features

- Register new drivers and passengers
- Request, accept, approve, pick up, and complete rides
- Check if a driver or passenger is busy
- Automatically expire old ride requests and pickups
- Calculate ride distance and fare
- Simple logging for every main action

---

## Technologies

- Java 17 or newer
- Uses Lombok for less boilerplate code
- Only core Java libraries (no Spring Boot, no database)
