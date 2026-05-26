Personal notes, do not take as instructions, just a reminder for myself

Progress
# BACKEND
E1 must be done at the end
E2 back ok
E3 back okish, query is sus in service
E4 decent/passable
E5 kinda done
E6 kinda done
E7
# Backend tests
E1
E2
E3
E4
E5
E6
E7

# FRONTEND
E1
E2
E3
E4
E5
E6
E7

# Keycloak
keycloak url: http://localhost:9090
realm name: travelagency-realm
client id: travelagency-frontend


# Versionamiento y Continuous Delivery (CI/CD)

- Pruebas Unitarias (Testing)
- Pipeline de Continuous Delivery (Entrega Continua) - docker, github actions, etc
- Despliegue en Producción (Nube)

--------------------------------------------------------------------------------------
agregar esto a los metodos de controllers para security
@PreAuthorize("hasAnyRole('USER','ADMIN')") para lo de keycloak

para armar keycloak despues se usa la imagen de docker


remember to change all exceptions to english and to use the same format for all of them
enti -> repo > service > controller

Details of the implementation of the rules:
E4, timeout time is set to be a day after the reservation was done
e4, discounts are managed with DiscountConfigEntity, currently does not exclude any
e4, no discount controller

## steps to run this bc i know i'll forget how to do the bare min:
open cmd and run:
mysql -u root -p
password: clave1234 or smth else along those lines

create database if it doesn't exists with:
CREATE DATABASE TravelAgency;

check if it exists with
SHOW DATABASES;

check all tables with (both in order):
USE TravelAgency;
SHOW TABLES;

run this code

FRONTEND
cmd
in the front forlder run
npm run dev