# 📌 Project : Resource Booking Platform (Hybrid Architecture)

## Overview
This project is a **resource booking platform** designed using a **hybrid architecture** that combines **Spring Boot microservices** with **AWS Serverless components**. The goal is to demonstrate how to apply the right architectural style to the right problem domain.

Core business logic is implemented as long-running services, while asynchronous, background, and event-driven workflows are handled using **AWS Lambda**.

## Key Features
- User authentication with JWT and refresh tokens
- Resource availability management
- Booking creation and cancellation
- Asynchronous notifications
- Automatic expiration of unpaid bookings
- Report generation

## Architecture
- **Backend (Microservices)**: Spring Boot (Java 17/21)
- **Serverless**: AWS Lambda
- **API Layer**: Amazon API Gateway
- **Messaging**: Amazon SQS / SNS
- **Database**: Amazon RDS (PostgreSQL)
- **Storage**: Amazon S3
- **Observability**: CloudWatch logs and metrics

## Event Flows
1. A booking is created via REST API.
2. A booking event is published to SQS.
3. A notification Lambda sends confirmation messages.
4. A scheduled Lambda expires unpaid bookings.
5. A reporting Lambda aggregates data into S3.

## Technical Focus
- Hybrid architecture design
- Event-driven communication
- Service-to-service boundaries
- Secure authentication mechanisms
- AWS Lambda integration with Spring Boot services


