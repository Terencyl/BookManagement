import http from 'k6/http';
import {Rate} from 'k6/metrics';
import { htmlReport } from "https://raw.githubusercontent.com/benc-uk/k6-reporter/2.4.0/dist/bundle.js";
import { textSummary } from "https://jslib.k6.io/k6-summary/0.0.1/index.js";

const failureRate = new Rate('failed requests');
const useCases = JSON.parse(open('./tests/usecases.json'));
const toCreate = JSON.parse(open('./tests/dataToCreate.json'));


// https://k6.io/docs/get-started/running-k6/
export function test_api_endpoints_config() {
    let useCase = useCases[Math.floor(Math.random() * useCases.length)];
    if (useCase.type === 'GET') {
        let res = http.get(`http://localhost:8080/books`);
        failureRate.add(res.status !== 200);
    } else {
        let res = http.post(
            `http://localhost:8080/books`,
            `
            {
                "name": "${useCase.name}",
                "author": "${useCase.author}"
            }
            `,
            {
                headers: {
                    'Content-Type': 'application/json'
                }
            }
        );
        failureRate.add(res.status !== 201);
    }
}

export function setup() {
    console.log("Setup started");

    toCreate.forEach(item =>
        http.post(
            `http://localhost:8080/books`,
            JSON.stringify(item),
            {
                headers: {
                    'Content-Type': 'application/json'
                }
            }
        )
    )

    console.log("Setup ended");
}

export function handleSummary(data) {
    return {
        "summary.html": htmlReport(data),
        stdout: textSummary(data, { indent: " ", enableColors: true }),
    };
}
