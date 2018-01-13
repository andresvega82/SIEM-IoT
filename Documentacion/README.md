# SEGURIDAD PARA IOT, UNA SOLUCIÓN PARA LA GESTIÓN DE EVENTOS DE SEGURIDAD EN ARQUITECTURAS DE INTERNET DE LAS COSAS


 

### 
### Arquitectura centinela IoT

![Imagen 2](https://github.com/andresvega82/SIEM-IoT/blob/master/Documentacion/Arquitectura_Centinela_IoT.jpg)


En la arquitectura de la solución propuesta del dispositivo centinela se cuenta con 3 módulos: Suricata IoT, Kismet y OpenVas; estos módulos representan cada herramienta que se usa para brindar protección a los dispositivos al alcance del centinela.

En primer lugar, el módulo de Suricata IoT, es una herramienta que permite utilizar reglas propias del software Suricata IDS (Sistema de Detección de Intruciones) con una modificación para la detección de tráfico de red propios de dispositivos IoT, cabe resaltar que este software solo sirve para monitorear el tráfico de red ethernet.

El segundo módulo es el de Kismet, que tiene cierta similitud con Suricata, es un IDS (Sistema de Detección de Intrusiones) especializado para detectar el tráfico de paquetes en redes inalámbricas, por ello, este software tiene un conjunto de reglas de detección de ataques especializado para redes inalámbricas.

Por último, el tercer módulo llamado OpenVas, es un software que permite analizar vulnerabilidades de los diferentes dispositivos basados en una base de datos actualizada de vulnerabilidades de diferentes dispositivos, entre los cuales, aplican los dispositivos IoT.

Los otros dos módulos restantes son para enviar eventos de seguridad a la plataforma de OSSIM para que puedan procesarse y generar las reglas de correlación que en el siguiente capítulo se explicaran.
