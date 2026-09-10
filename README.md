# CimdProxy

_A service that listens for CIMD traffic from MobilityGuard to send SMS, primarily regarding 2FA passwords. It uses
SmsSender as the SMS provider._

## Getting Started

### Prerequisites

- **Java 25 or higher**
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

   If this microservice depends on other services, make sure they are up and accessible.
   See [Dependencies](#dependencies) for more details.

4. **Build and run the application:**

```bash
mvn spring-boot:run
```

## Dependencies

This microservice does not depend on any other internal services. However, it does depend on external services for the
provider(s) it intends to use:

- **SmsSender**
  - **Purpose:** Is used to send text messages.
  - **Repository:
    ** [https://github.com/Sundsvallskommun/api-service-sms-sender](https://github.com/Sundsvallskommun/api-service-sms-sender.git)
  - **Setup Instructions:** See documentation in repository above for installation and configuration steps.

## API Documentation

This service does not expose an API

## Usage

N/A

## Configuration

Configuration is crucial for the application to run successfully. Ensure all necessary settings are configured in
`application.yml`.

### Key Configuration Parameters

- **CIMD Port:**

```yaml
cimd:
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

### TLS certificates

The CIMD listener terminates TLS itself. Enable it with `cimd.ssl.enabled` and supply the certificate through a Spring
SSL bundle named `server`:

```yaml
cimd:
  ssl:
    enabled: true

spring:
  ssl:
    bundle:
      pem:
        server:
          keystore:
            certificate: <certificate>
            private-key: <private key>
```

The bundle names matter. `CIMD` looks up `server` for the server certificate, and `client` for a truststore. Adding a
`client` bundle switches the listener to two-way SSL, where the client must present a certificate signed by something in
that truststore. Without a `client` bundle, client certificates are not verified.

Both properties accept a file location (`file:/path/to/cert.pem`), inline PEM content, or the PEM content base64 encoded
and prefixed with `base64:`. Use file locations when running locally.

### Updating the certificate

In OpenShift the values come from a `Secret`, which means they are encoded twice. `Secret.data` requires base64, and
Kubernetes decodes that layer before the application sees the value. What remains is the `base64:` form above, which
exists so that multi-line PEM content fits on a single line. Encode the PEM text as it is, `BEGIN` and `END` markers
included. DER encoded input does not work.

First check that the certificate and the key belong together. The two hashes must be identical:

```bash
openssl x509 -noout -modulus -in new.crt | openssl sha256
openssl rsa  -noout -modulus -in new.key | openssl sha256
```

Then check the key format. `BEGIN PRIVATE KEY` works as is. If the file says `BEGIN ENCRYPTED PRIVATE KEY`, either set
`spring.ssl.bundle.pem.server.keystore.private-key-password`, or remove the password:

```bash
openssl pkcs8 -topk8 -nocrypt -in new.key -out new.pk8.key
```

If the certificate needs a chain, concatenate the leaf certificate and the intermediates into one file before encoding.
The `certificate` property accepts several PEM blocks.

Finally build the two values to paste into `Secret.data`/`secret.yaml`:

```bash
printf 'base64:%s' "$(base64 < new.crt)" | base64 | tr -d '\n'
printf 'base64:%s' "$(base64 < new.pk8.key)" | base64 | tr -d '\n'
```

The `$(...)` and the `tr -d '\n'` both strip newlines, and neither is optional. A trailing newline inside the inner
base64 makes Spring reject the value with `IllegalArgumentException: Input byte array has incorrect ending byte`, and
the application will not start.

### Verifying the certificate

The startup log reports what the listener actually does, not what was configured:

```
CIMD listening on port 9971 (TLS)
CIMD listening on port 9971 (plaintext)
```

`(plaintext)`, or a `WARN` about a missing `server` bundle, means the certificate was not loaded.

`/actuator/health` reports the configured bundles and the certificate validity. The health indicator reports
`OUT_OF_SERVICE` before a certificate expires, controlled by
`management.health.ssl.certificate-validity-warning-threshold`.

To check what the running service presents:

```bash
openssl s_client -showcerts -connect <host>:9971
```

For a self-signed certificate, add `-CAfile <cert-file>`.

## Code status

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_cimd-proxy&metric=alert_status)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_cimd-proxy)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_cimd-proxy&metric=reliability_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_cimd-proxy)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_cimd-proxy&metric=security_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_cimd-proxy)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_cimd-proxy&metric=sqale_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_cimd-proxy)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_cimd-proxy&metric=vulnerabilities)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_cimd-proxy)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_cimd-proxy&metric=bugs)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_cimd-proxy)

## 

Copyright (c) 2025 Sundsvalls kommun
