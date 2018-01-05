
                                                 INSTALACIÓN DE KISMET
                                                  
 Para la instalar kismet en Linux solo es escribir los siguientes comandos en la terminal:
-	Sudo apt-get update
-	Sudo apt-get Install kismet
Como el IDS Kismet fue instalado en una Raspberry Pi 3, es importarte saber que la tarjeta de red de este dispositivo no se deja cambiar a modo monitor, por lo tanto fue necesario implementarle la siguiente antena USB:

Imagen 1.
 
Configuración de la antena: Para cambiar la tarjeta a modo monitor se escriben los siguientes comandos en la consola:
-  sudo ifconfig wlan0 down
-  sudo iwconfig wlan0 mode monitor
-  sudo ifconfig wlan0 up
*La interface es a la que está conectada la antena, para mirar escriba el comando: ifconfig

Configuración de kismet:
Vamos al archivo de configuración de kismet  kismet.conf que generalmente se encuentra en la ruta /etc/kismet/kimet.conf y la abrimos con el siguiente comando:
-	nano kismet.conf
Añadimos al final del archivo “ncsource = wlan0” sin las “”, para que kismet reconozca por de interface se va hacer el escaneo.
Corriendo Kismet:
Para correr kismet ese escribe el comando:
-	kismet
Se abrirá una ventana de kismet:
 
Imagen 2.

Si queremos ver la información por la consola de kismet le damos click en []Show Console 
Le damos click en Start.
Y kismet empieza a monitorer las redes WIFI. 
 
Imagen 3.

CONFIGURACIÓN KISMET EN OSSIM
Para cuando Kismet genere la alerta y la pueda enviar a OSSIM se debe tener corriendo el programa que está en el siguiente Github https://github.com/andresvega82/SIEM-IoT/tree/master/Software/Kismet
en el archivo Syslogkismet.zip,  el cual envía el syslog al OSSIM para que este pueda recibir el evento.
En la siguiente imagen podemos ver en color amarillo el código donde se envía el syslog: 
 
Imagen 4.
**Es necesario incluir las clases:
 
**Para correr el programa se genera un .jar
** y se ejecuta con el conmando java -jar <el nombre del archivo >.Jar.

Ya con lo anterior se envía el syslog al OSSIM , pero para indicarles que campos del syslog queremos que se vea en el evento, es necesario agregar en la plataforma de OSSIM le plugin kismetIoTPlugin.cfg, el cual se encuentra en el Github mencionado anteriormente, el cual contiene la expresión regular:
 
Imagen 5.
Continuación de la expresión regular
 
Imagen 6.
La cual hará que el OSSIM reciba y muestre el evento de la siguiente forma:
Imagen 7.
PRUBAR KISMET

Generar una alerta en Kismet:
En este escenario vamos a generar la alerte BCASTDISCON la cual se dispara cuando detecta que hay un ataque de desasociación de la red de un cliente o de varios, causando una posible denegación de servicio.
Debemos tener el Kismet corriendo:
Comando : Kismet
Para generar el ataque vamos a utilizar la herramienta aireplay-ng, en nuestro caso vamos a desconectar a todos los clientes conectados a la red:
Escribimos el siguiente comando:
-	aireplay-ng –deauth 0 –a < BSSID> wlan1

*el 0(cero) indica que va a realizar el ataque indefinidamente, si queremos que solo se realice 1 vez, cambiamos el 0 por 1.
* wlan1 es la interface de red.
* El BSSID es el punto de acceso por el cual se va a realizar el ataque.
 Imagen 8.

Cualquiera que se encuentre en la red para visualizar mejor estas BSSID podemos utilizar el comando: airodump-ng wlan1
, utilizando la interface de red que tengamos en el momento.

Luego le damos Enter y se empieza el ataque, se demora un poco ya que tiene que coincidir el canal(CH) del BSSID con el de la interface de red(wlan1). 
 
Imagen 9.

En la imagen anterior ponemos ver que empieza a enviar paquetes de desasociación por el punto que acceso que definimos y de este modo generar una denegación de servicio.

Kismet detecta el ataque:
 
Imagen 10.
Informado que tipo de ataque es, por donde se está generando el ataque y cual la consecuencia de este.
Cuando kismet recibe la alerta esta es reportada al OSSIM y el OSSIM por medio del siguiente script, realiza una acción:
 
Imagen 11.
En nuestro caso lo que hace el reiniciar el equipo cerrando el punto de acceso del atacante. En el OSSIM se programa de la siguiente manera:
 COMMAND:* se indica el scripts que se va a utilizar.
En TO* se puede poner un correo para informar al administrador de que ocurrió el evento.

