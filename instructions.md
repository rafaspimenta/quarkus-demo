Backend Fight Instructions - 2025
Challenge
Your team and/or you need to develop a backend that intermediates payment requests to a payment processing service, called a Payment Processor.

diagram

For each payment brokered, a finance fee is charged. For example, with a 5% fee for a $100.00 payment request, you would be charged $5.00 and would be left with $95.00.

However, since life is truly tough, this service will experience instability. Response times may become very high, and it may even become unavailable, responding with HTTP 500 errors. And since we know life is tough, we prepare for these situations and create a plan B. Plan B is a second Payment Processor service.

diagram

Note : Payment processing fees will not change during testing and the default service will always have the lowest fee.

The problem is that this contingency service—called Payment Processor Fallback—charges a higher fee on payments. And it can also experience instability and unavailability! In fact, both services may become unstable and/or unavailable at the same time, because that's life...

Nothing is so bad that it can't get worse...
In addition to the endpoint POST /payments, you'll also need to provide an endpoint detailing the summary of processed payments GET /payments-summary. This endpoint will be used to audit the consistency between what was processed by your backend and what was processed by the two Payment Processors. This is the Central Bank periodically verifying that you're recording everything correctly.

diagram

These periodic calls during the Rinha test will buy the answers and, for each inconsistency, a hefty fine will be applied!

Life is a Roller Coaster...
To make your life easier and to check the availability of Payment Processors, each one provides a health-check endpoint – GET /payments/service-healthwhich shows whether the service is experiencing outages and the minimum response time for payment processing. However, this endpoint has a limit of one call every five seconds. If this limit is exceeded, a response HTTP 429 - Too Many Requestswill be returned. You can use these endpoints to develop the best strategy for paying the lowest possible fee.

Score
The Backend Battle scoring criteria will be how much profit your backend generated at the end of the test. In other words, the more payments you make with the lowest financial rate, the better. Remember that if any inconsistencies are detected by the Central Bank, you'll have to pay a 35% fine on your total profit.

There's a technical scoring criterion as well. If your backend and Payment Processors have very fast response times, you can also score points. The performance metric used will be p99 (we'll take the worst 1% response times - 99th percentile). From a p99 of 10ms or less, you receive a bonus of 2% on your total profit for every 1ms below 11ms.

The formula for the performance bonus percentage is (11 - p99) * 0,02. If the value is negative, the bonus is 0%—there is no penalty for results with p99 greater than 11ms.

Examples:

10ms p99 = 2% bonus
p99 of 9ms = 4% bonus
5ms p99 = 12% bonus
p99 of 1ms = 20% bonus
¹ The percentile will be calculated based on all HTTP requests made in the test and not just on requests made to your backend.

² All payments will be exactly the same amount – no random amounts will be generated.

Architecture, Constraints and Submission
Your backend must follow the following architecture/constraints.

Web Servers : Have at least two web server instances that will respond to requests POST /payments. GET /payments-summaryIn other words, some form of load distribution must occur (usually through a load balancer such as nginx, for example).

Containerization : You must make your backend available in docker compose format. All images declared in docker compose ( docker-compose.yml) must be publicly available in image registries ( https://hub.docker.com/ for example).

You must restrict CPU and Memory usage to 1.5 CPU units and 350MB of memory among all services declared as you wish through attributes deploy.resources.limits.cpusand deploy.resources.limits.memoryas in the example in the following snippet.

services:
  seu-servico:
    ...
    deploy:
      resources:
        limits:
          cpus: "0.15"
          memory: "42MB"
Examples docker-compose.yml here , here and here .

Port 9999 : Your endpoints should be exposed on the port 9999accessible via http://localhost:9999– example here .

Other restrictions
Images must be linux-amd64 compatible.
Network mode must be bridge – host mode is not allowed.
Privileged mode is not allowed .
The use of replicated services is not permitted – this makes it difficult to verify the resources used.
Submission
Important! : The deadline to submit your backend is 2025-08-17 until 23:59:59 ! The results are expected to be released on 2025-08-20 .

To have your backend officially tested by Backend Rhine, see the results compared to other submissions, and have your name listed as a participant, you'll need to do the following:

Have a public git repository (github, for example) with the source code and all artifacts related to the Rinha submission.

Open a PR on this repository by adding a directory with your ID to participants . In this PR, you must:

Include an README.mdexplanation of the technologies used and a link to the repository with the source code for your submission.
Include the file docker-compose.ymlin the root of this repository with its dependencies (database scripts, configurations, etc.).
Include a file info.jsonwith the following structure to facilitate the collection of technologies used:
{
    "name": "Débora Nis Zanfranceschi",
    "social": ["https://x.com/debora-zan", "https://bsky.app/profile/debora-zan.bsky.social"],
    "source-code-repo": "https://github.com/debora-zan/rinha-de-backend-2025",
    "langs": ["node"],
    "storages": ["postgresql", "redis"],
    "messaging": ["rabbitmq", "nats"],
    "load-balancers": ["nginx"],
    "other-technologies": ["xpto"] // inclua qq coisa que não se encaixe nas outra categorias
}
Example file structure of a submission PR:
├─ participantes/
|  ├─ debs-node-01/
|  |  ├─ docker-compose.yml
|  |  ├─ info.json
|  |  ├─ nginx.config
|  |  ├─ sql/
|  |  |  ├─ ddl.sql
|  |  |  ├─ dml.sql
|  |  ├─ README.md
Important!

Do not include source code in the submission.
Do not include logs in the submission.
Include ONLY what is necessary to run the tests.
Be aware of CPU and memory restrictions.
And How Do I Integrate with Payment Processors???
Payment Processors will also run in containers through the file docker-compose.yml(or docker-compose-arm64.ymlfor hosts using ARM64 processors like newer MacOS models). The files for containerizing them are in this directory .

It's important to note that you docker-compose.ymlmust declare the network used in your Payment Processors. This means you must first upload the Payment Processors so their network can be created before you upload your backend. You'll also need to include the network payment-processorin the services that will integrate with the Payment Processors, as in this example . Don't forget to also declare the network payment-processorin your file docker-compose.yml, as in this example .

Once everything is configured in terms of networks, both Payment Processors will be available at the following addresses for their services:

Payment Processor Default in http://payment-processor-default:8080 Payment Processor Fallback inhttp://payment-processor-fallback:8080

Now just make a reference to these two addresses, as in this example .

These two addresses are also accessible via host so you can access them to explore at the following addresses:

Payment Processor Default at http://localhost:8001 Payment Processor Fallback at http://localhost:8002

Testing Locally
Follow these instructions to test your backend locally.

Leonardodosegfault kindly put together a mini setup guide to help with local setup for running tests.

Endpoint Details
Endpoints that your Backend Should Provide
Payments
Main endpoint that receives payment requests to be processed.

POST /payments
{
    "correlationId": "4a7901b8-7d26-4d9d-aa19-4dc1c7cf60b3",
    "amount": 19.90
}

HTTP 2XX
Qualquer coisa
request

correlationIdis a mandatory and unique field of type UUID.
amountis a mandatory field of type decimal.
response

Any response in the 2XX range (200, 201, 202, etc.) is valid. The response body will not be validated – it can be anything or even empty.
Payments Summary
This endpoint needs to return a summary of what has already been processed in terms of payments.

GET /payments-summary?from=2020-07-10T12:34:56.000Z&to=2020-07-10T12:35:56.000Z

HTTP 200 - Ok
{
    "default" : {
        "totalRequests": 43236,
        "totalAmount": 415542345.98
    },
    "fallback" : {
        "totalRequests": 423545,
        "totalAmount": 329347.34
    }
}
request

fromis an optional timestamp field in ISO format in UTC (usually 3 hours ahead of Brazil time).
tois an optional timestamp field in ISO format in UTC.
response

default.totalRequestsis a mandatory field of type integer.
default.totalAmountis a mandatory field of type decimal.
fallback.totalRequestsis a mandatory field of type integer.
fallback.totalAmountis a mandatory field of type decimal.
Important! This endpoint, along with the Payment Processors' Payment Summary , will be called several times during the test to check consistency. The values must be consistent, otherwise, inconsistency penalties will be applied.

Endpoints that Payment Processors Provide
Your backend must integrate with two Payment Processors. Both services have identical APIs, so the following descriptions apply to both.

Payments
This endpoint receives and processes payments—it's similar to the Payments endpoint your backend needs to provide. It's the main endpoint you need to integrate with your backend.

POST /payments
{
    "correlationId": "4a7901b8-7d26-4d9d-aa19-4dc1c7cf60b3",
    "amount": 19.90,
    "requestedAt" : "2025-07-15T12:34:56.000Z"
}

HTTP 200 - Ok
{
    "message": "payment processed successfully"
}
request

correlationIdis a mandatory and unique field of type UUID.
amountis a mandatory field of type decimal.
requestedAtis a mandatory field of the timestamp type in ISO format in UTC.
response

messageis an always present field of type text.
Health Check
This endpoint allows you to check the conditions of the Payments endpoint . This endpoint in both Payment Processor services can help you decide which is the best option for processing a payment.

GET /payments/service-health

HTTP 200 - Ok
{
    "failing": false,
    "minResponseTime": 100
}
request - There are no request parameters. However, this endpoint imposes a call limit – 1 call every 5 seconds. If this limit is exceeded, you will receive an HTTP 429 error response - Too Many Requests.

response

failingis an always-present Boolean field that indicates whether the Payments endpoint is available. If it is not, requests to the endpoint will receive errors HTTP5XX.
minResponseTimeis an always-present integer field indicating the best possible response time for the Payments endpoint . For example, if the returned value is 100, there will be no responses faster than 100ms.
Payment Details
You don't need to integrate with this endpoint. It's for troubleshooting purposes if you want/need it.

GET /payments/{id}

HTTP 200 - Ok
{
    "correlationId": "4a7901b8-7d26-4d9d-aa19-4dc1c7cf60b3",
    "amount": 19.90,
    "requestedAt" : 2025-07-15T12:34:56.000Z
}
request - {id}is a mandatory parameter of the UUID type.

response

correlationIdis an ever-present field of type UUID.
amountis an always present field of type decimal.
requestedAtis an ever-present timestamp field in ISO format in UTC.
Payment Processor Administrative Endpoints
Payment Processor services have administrative endpoints. These endpoints will be used during testing by the TEST SCRIPT and you should not integrate with them in the final version. However, they can be useful for simulating failures, long response times, checking consistency, etc. All of the following endpoints require a token that must be provided in the X-Rinha-Tokenrequest header.

Payments Summary
This endpoint is similar to the Payments Summary endpoint that you need to develop in your backend.

GET /admin/payments-summary?from=2020-07-10T12:34:56.000Z&to=2020-07-10T12:35:56.000Z

HTTP 200 - Ok
{
    "totalRequests": 43236,
    "totalAmount": 415542345.98,
    "totalFee": 415542.98,
    "feePerTransaction": 0.01
}
request

fromis an optional timestamp field in ISO format in UTC.
tois an optional timestamp field in ISO format in UTC.
response

totalRequestsis an always-present integer numeric field. It shows how many payments were processed in the selected period, or all payments if the period is not specified.
totalAmountis an always present decimal field. It shows the sum of all payments processed in the selected period or the sum of all payments if the period is not specified.
totalFeeis an always present decimal field. It shows the sum of the fees for payments processed in the selected period, or the sum of the fees for payments if the period is not specified.
feePerTransactionis an ever-present decimal field. It displays the fee amount per transaction.
Important! This endpoint, along with the Payments Summary endpoint that your backend must provide, will be called several times during the test to check consistency. The values must be consistent, otherwise, there will be a penalty for inconsistency.

Set Token
This endpoint sets a password for the administrative endpoints. If you change the password in your final submission, the test will be aborted and you will lose points in the Brawl. The initial password is [unclear] 123and you can use it to run local tests.

PUT /admin/configurations/token
{
    "token" : "uma senha qualquer"
}

HTTP 204 - No Content
request

tokenis a mandatory text field.
response

N/A
Set Delay
This endpoint sets a purposeful delay on the Payments endpoint to simulate a longer response time.

PUT /admin/configurations/delay
{
    "delay" : 235
}

HTTP 204 - No Content
request

delayis a mandatory field of type integer to define the milliseconds of delay in the response time in the Payments endpoint .
response

N/A
Set Failure
This endpoint sets a purposeful failure in the Payments endpoint to simulate server errors.

PUT /admin/configurations/failure
{
    "failure" : true
}

HTTP 204 - No Content
request

failureis a mandatory boolean field to define whether the Payments endpoint will return a failure.
response

N/A
Database Purge
This endpoint deletes all payments from the database and is only for ease of development.

POST /admin/purge-payments

HTTP 200 - Ok
{
    "message": "All payments purged."
}
request

N/A
response

messageis an ever-present text-type field.
Endpoint Summary
The tables below provide a summary to facilitate an overview of the solution.

Endpoints to be developed

Endpoint	Description
POST /payments	Intermediates the request for payment processing.
GET /payments-summary	Displays details of payment processing requests.
Endpoints available in both Payment Processor services

Endpoint	Description
POST /payments	Requests processing of a payment.
GET /payments/service-health	Checks the payment endpoint's operating conditions. Limit 1 call every 5 seconds.
GET /payments/{id}	Displays details of a payment processing request.
GET /admin/payments-summary	Displays details of payment processing requests.
PUT /admin/configurations/token	Resets an access token required for all endpoints prefixed with '/admin/'
PUT /admin/configurations/delay	Configures the delay on the payments endpoint.
PUT /admin/configurations/failure	Configures failure in the payments endpoint.
POST /admin/purge-payments	Deletes all payments from the database. For development purposes only.
Other Information
The Payment Processor source code is available here : https://github.com/zanfranceschi/rinha-de-backend-2025-payment-processor