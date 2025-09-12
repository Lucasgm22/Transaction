import http from 'k6/http';
import { check, sleep } from 'k6';

// --- Configuração do Teste ---
export const options = {
  stages: [
    { duration: '30s', target: 100 },
    { duration: '2m', target: 100 },
    { duration: '30s', target: 0 },
  ],
  thresholds: {
    'http_req_duration': ['p(95)<250'],
    'http_req_failed': ['rate<0.01'],
  },
};

const API_BASE_URL = 'http://localhost:8080';
const HEADERS = { 'Content-Type': 'application/json' };

// --- Dados e Funções de Apoio ---

// Pool de moedas para variar as requisições GET
const CURRENCIES = ['Brazil-Real', 'Japan-Yen', 'Canada-Dollar', 'United%20Kingdom-Pound', 'Mexico-Peso'];

/**
 * Gera uma data aleatória nos últimos 365 dias no formato YYYY-MM-DD.
 */
function getRandomDate() {
  const today = new Date();
  const pastDate = new Date();
  pastDate.setDate(today.getDate() - Math.floor(Math.random() * 365));
  return pastDate.toISOString().slice(0, 10);
}

// --- Fase de Setup ---
export function setup() {
  console.log('Setting up initial data pool...');
  const transactionIds = [];
  for (let i = 0; i < 50; i++) {
    const payload = JSON.stringify({
      description: `Setup Transaction ${i}`,
      transactionDate: getRandomDate(), // <-- Usando data aleatória no setup
      purchaseAmount: 50 + i
    });
    const res = http.post(`${API_BASE_URL}/transaction`, payload, { headers: HEADERS });
    if (res.status === 201) {
        transactionIds.push(res.json('id'));
    }
  }
  console.log(`Setup complete. ${transactionIds.length} initial transactions created.`);
  return { ids: transactionIds };
}

// --- Fase de Carga (Workload Misto e Variado) ---
export default function (data) {
  const random = Math.random();

  // 80% de chance de fazer uma requisição GET (Leitura)
  if (random < 0.80) {
    // Pega um ID e uma moeda aleatórios
    const randomId = data.ids[Math.floor(Math.random() * data.ids.length)];
    const randomCurrency = CURRENCIES[Math.floor(Math.random() * CURRENCIES.length)];

    const res = http.get(`${API_BASE_URL}/transaction/${randomId}?currency=${randomCurrency}`);
    check(res, {
      'GET response is valid (200 OK or expected 404 Not Found)': (r) => {
        const isSuccess = r.status === 200;
        const isExpectedBusinessError = r.status === 404 && r.body.includes('Could not retrieve exchange rates');
        return isSuccess || isExpectedBusinessError;
      },
    });
  }
  // 20% de chance de fazer uma requisição POST (Escrita)
  else {
    const payload = JSON.stringify({
      description: `Mixed Load Write Test - VU ${__VU} Iter ${__ITER}`,
      transactionDate: getRandomDate(), // <-- Usando data aleatória
      purchaseAmount: 123.45
    });
    const res = http.post(`${API_BASE_URL}/transaction`, payload, { headers: HEADERS });
    check(res, { 'POST status was 201': (r) => r.status === 201 });
  }

  sleep(1)
}