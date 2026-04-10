Запуск всего:
1) docker compose up -d -build (если ничего не меняли в коде то просто up -d без билда)
2) Ждём пока всё запустится
3) Если запускаем первый раз/запускаем после docker compose down - переходим в корень репозитория и в терминале:
docker exec kafka /opt/kafka/bin/kafka-topics.sh --create --topic purchases --bootstrap-server localhost:29092 --partitions 1 --replication-factor 1 --if-not-exists