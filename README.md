# CimdProxy

_A service that listens for CIMD traffic from MobilityGuard to send SMS, primarily regarding 2FA passwords. It uses SmsSender as the SMS provider._

## Getting Started

### Prerequisites

- **Java 21 or higher**
- **Maven**
- **Git**
- **[Dependent Microservices](#dependencies)**

### Installation

1. **Clone the repository:**

```bash
git clone https://github.com/Sundsvallskommun/cimd-proxy.git
cd cimd-proxy
```

2. **Configure the application:**

   Before running the application, you need to set up configuration settings.
   See [Configuration](#configuration)

   **Note:** Ensure all required configurations are set; otherwise, the application may fail to start.

3. **Ensure dependent services are running:**

   If this microservice depends on other services, make sure they are up and accessible. See [Dependencies](#dependencies) for more details.

4. **Build and run the application:**

   - Using Maven:

```bash
mvn spring-boot:run
```

- Using Gradle:

```bash
gradle bootRun
```

## Dependencies

This microservice does not depend on any other internal services. However, it does depend on external services for the provider(s) it intends to use:

- **SmsSender**
  - **Purpose:** Is used to send text messages.
  - **Repository:** [https://github.com/Sundsvallskommun/api-service-sms-sender](https://github.com/Sundsvallskommun/api-service-sms-sender.git)
  - **Setup Instructions:** See documentation in repository above for installation and configuration steps.

## API Documentation

This service does not expose an API

## Usage

N/A

## Configuration

Configuration is crucial for the application to run successfully. Ensure all necessary settings are configured in `application.yml`.

### Key Configuration Parameters

- **Server Port:**

```yaml
server:
  port: 9971
```

- **External Service URLs**

```yaml
integration:
  sms-sender:
    municipality-id: <your-municipality-id>
    base-url: <base-url>
    oauth2:
      token-url: <token-url>
    sms:
      from: <sender-alias>
```

## Code status

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_cimd-proxy&metric=alert_status)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_cimd-proxy)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_cimd-proxy&metric=reliability_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_cimd-proxy)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_cimd-proxy&metric=security_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_cimd-proxy)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_cimd-proxy&metric=sqale_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_cimd-proxy)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_cimd-proxy&metric=vulnerabilities)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_cimd-proxy)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_cimd-proxy&metric=bugs)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_cimd-proxy)

## 

Copyright (c) 2025 Sundsvalls kommun
