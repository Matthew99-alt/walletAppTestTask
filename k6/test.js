import http from 'k6/http';
import { check } from 'k6';

export const options = {
  vus: 50,
  duration: '30s',
};

const BASE_URL = (__ENV.BASE_URL || 'http://localhost:8080') + '/api/v1';
const HEADERS = { headers: { 'Content-Type': 'application/json' } };

// Все VU бьют в один кошелёк — проверяем поведение под конкурентной записью.
export function setup() {
  const walletId = crypto.randomUUID();

  const res = http.post(
    `${BASE_URL}/wallets`,
    JSON.stringify({ walletId: walletId, amount: 0 }),
    HEADERS,
  );

  check(res, { 'wallet created': (r) => r.status === 201 });

  return { walletId: walletId };
}

export default function (data) {
  const payload = JSON.stringify({
    walletId: data.walletId,
    operationType: 'DEPOSIT',
    amount: 1,
  });

  const res = http.post(`${BASE_URL}/wallet`, payload, HEADERS);

  check(res, { 'status is 200': (r) => r.status === 200 });
}

// После теста баланс должен равняться числу успешных депозитов
// (метрика checks в отчёте k6): потерянных обновлений быть не должно.
export function teardown(data) {
  const res = http.get(`${BASE_URL}/wallets/${data.walletId}`);
  console.log(`Final wallet state: ${res.body}`);
}
