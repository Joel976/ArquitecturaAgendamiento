#!/bin/bash

echo "🛑 Deteniendo contenedores..."
docker-compose down

echo "🔥 Eliminando volúmenes..."
docker volume rm \
  hospital_agendamiento_data \
  hospital_pacientes_data \
  hospital_medicos_data \
  hospital_notificaciones_data

echo "🔄 Reconstruyendo microservicios..."
docker-compose up --build