Работай в режиме read-only. Код не изменяй.

Задача:
понять, как внедрить Space между Hub и Room.

Исследуй:
- Room entity и lifecycle;
- Message → Room relation;
- CallSession → Room relation;
- room REST API;
- room WebSocket events;
- Redux room state;
- маршрутизацию frontend.

Не исследуй:
- audio processing;
- QoE adaptation;
- code-runner internals;
если они не имеют прямой зависимости от Room.

Верни:
1. Текущую модель.
2. Точки интеграции.
3. Скрытые предположения.
4. Риски миграции.
5. 2–3 варианта архитектуры.
6. Рекомендацию.
7. Порядок реализации.

Для каждого вывода укажи файлы и символы, на которых он основан.
