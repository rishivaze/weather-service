# Weather Forecast Service

Java web application that retrieves weather forecasts for US ZIP codes with 30-minute caching.

## Features

- Enter ZIP code to get current temperature, high, low, and extended forecast
- 30 minute cache with visual indicator (green = cached, orange = fresh)


## Quick Start

```
# Build and run:
mvn clean compile
mvn spring-boot:run

# Access at http://localhost:8080
```

## Usage

1. Open `http://localhost:8080`
2. Enter a 5-digit ZIP code (e.g., 10001)

