




                                                 INSTALACIÃ“N DE KISMET
                                                  
 Para la instalar kismet en Linux solo es escribir los siguientes comandos en la terminal:
-	Sudo apt-get update
-	Sudo apt-get Install kismet
Como el IDS Kismet fue instalado en una Raspberry Pi 3, es importarte saber que la tarjeta de red de este dispositivo no se deja cambiar a modo monitor, por lo tanto fue necesario implementarle la siguiente antena USB:

![GitHub 1](/Kismet/1.jpg)
 
ConfiguraciÃ³n de la antena: Para cambiar la tarjeta a modo monitor se escriben los siguientes comandos en la consola:
-  sudo ifconfig wlan0 down
-  sudo iwconfig wlan0 mode monitor
-  sudo ifconfig wlan0 up
*La interface es a la que estÃ¡ conectada la antena, para mirar escriba el comando: ifconfig

ConfiguraciÃ³n de kismet:
Vamos al archivo de configuraciÃ³n de kismet  kismet.conf que generalmente se encuentra en la ruta /etc/kismet/kimet.conf y la abrimos con el siguiente comando:
-	nano kismet.conf
AÃ±adimos al final del archivo â€œncsource = wlan0â€ sin las â€œâ€, para que kismet reconozca por de interface se va hacer el escaneo.
Corriendo Kismet:
Para correr kismet ese escribe el comando:
-	kismet
Se abrirÃ¡ una ventana de kismet:
 
Imagen 2.

Si queremos ver la informaciÃ³n por la consola de kismet le damos click en []Show Console 
Le damos click en Start.
Y kismet empieza a monitorer las redes WIFI. 
 
Imagen 3.

CONFIGURACIÃ“N KISMET EN OSSIM
Para cuando Kismet genere la alerta y la pueda enviar a OSSIM se debe tener corriendo el programa que estÃ¡ en el siguiente Github https://github.com/andresvega82/SIEM-IoT/tree/master/Software/Kismet
en el archivo Syslogkismet.zip,  el cual envÃ­a el syslog al OSSIM para que este pueda recibir el evento.
En la siguiente imagen podemos ver en color amarillo el cÃ³digo donde se envÃ­a el syslog: 
 
Imagen 4.
**Es necesario incluir las clases:
 
**Para correr el programa se genera un .jar
** y se ejecuta con el conmando java -jar <el nombre del archivo >.Jar.

Ya con lo anterior se envÃ­a el syslog al OSSIM , pero para indicarles que campos del syslog queremos que se vea en el evento, es necesario agregar en la plataforma de OSSIM le plugin kismetIoTPlugin.cfg, el cual se encuentra en el Github mencionado anteriormente, el cual contiene la expresiÃ³n regular:
 
Imagen 5.
ContinuaciÃ³n de la expresiÃ³n regular
 
Imagen 6.
La cual harÃ¡ que el OSSIM reciba y muestre el evento de la siguiente forma:
Imagen 7.
PRUBAR KISMET

Generar una alerta en Kismet:
En este escenario vamos a generar la alerte BCASTDISCON la cual se dispara cuando detecta que hay un ataque de desasociaciÃ³n de la red de un cliente o de varios, causando una posible denegaciÃ³n de servicio.
Debemos tener el Kismet corriendo:
Comando : Kismet
Para generar el ataque vamos a utilizar la herramienta aireplay-ng, en nuestro caso vamos a desconectar a todos los clientes conectados a la red:
Escribimos el siguiente comando:
-	aireplay-ng â€“deauth 0 â€“a < BSSID> wlan1

*el 0(cero) indica que va a realizar el ataque indefinidamente, si queremos que solo se realice 1 vez, cambiamos el 0 por 1.
* wlan1 es la interface de red.
* El BSSID es el punto de acceso por el cual se va a realizar el ataque.
 Imagen 8.

Cualquiera que se encuentre en la red para visualizar mejor estas BSSID podemos utilizar el comando: airodump-ng wlan1
, utilizando la interface de red que tengamos en el momento.

Luego le damos Enter y se empieza el ataque, se demora un poco ya que tiene que coincidir el canal(CH) del BSSID con el de la interface de red(wlan1). 
 
Imagen 9.

En la imagen anterior ponemos ver que empieza a enviar paquetes de desasociaciÃ³n por el punto que acceso que definimos y de este modo generar una denegaciÃ³n de servicio.

Kismet detecta el ataque:
 
Imagen 10.
Informado que tipo de ataque es, por donde se estÃ¡ generando el ataque y cual la consecuencia de este.
Cuando kismet recibe la alerta esta es reportada al OSSIM y el OSSIM por medio del siguiente script, realiza una acciÃ³n:
 
Imagen 11.
En nuestro caso lo que hace el reiniciar el equipo cerrando el punto de acceso del atacante. En el OSSIM se programa de la siguiente manera:
 COMMAND:* se indica el scripts que se va a utilizar.
En TO* se puede poner un correo para informar al administrador de que ocurriÃ³ el evento.

						INSTALACIÃ“N DE SURICATA IOT
												
1.	Lista que librerÃ­as para instalar sobre SO:
	sudo apt-get -y install libpcre3 libpcre3-dbg libpcre3-dev \
	build-essential autoconf automake libtool libpcap-dev libnet1-dev \
	libyaml-0-2 libyaml-dev pkg-config zlib1g zlib1g-dev libcap-ng-dev libcap-ng0 \
	make libmagic-dev libjansson-dev

	wget https://github.com/OISF/libhtp/archive/0.5.21.tar.gz
	tar -xzvf 0.5.21.tar.gz
	cd libhtp
	./autogen.sh
	./configure
	make
	make install
	ldconfig

2.	Descargar comprimido de Suricata:
	git clone https://github.com/decanio/suricata-IoT.git

3.	Entrar a la carpeta de suricata:
	cd suricata-3.1

4.	Instalar:
	./autogen.sh
	./configure && make && make install-full
	ldconfig

5.	Copiar archivos de configuraciÃ³n:
	mkdir /var/log/suricata
	mkdir /etc/suricata
	mkdir /etc/suricata/rules
	cp classification.config /etc/suricata
	cp reference.config /etc/suricata
	cp suricata.yaml /etc/suricata

6.	Bajar reglas (por defecto se bajan por Oinkmaster):
	apt-get install oinkmaster
	editar el archivo oinkmaster.conf: /etc/oinkmaster.conf
	adicionar lÃ­nea: 
	url = http://rules.emergingthreats.net/open/suricata/emerging.rules.tar.gz
	comando: oinkmaster -C /etc/oinkmaster.conf -o /etc/suricata/rules

7.	Correr Suricata:
	suricata -c /etc/suricata/suricata.yaml -i eth0 --init-errors-fatal
	
	Archivo de configuraciÃ³n suricata.yaml

El archivo de configuraciÃ³n de Suricata IoT es el archivo llamado suricata.yaml, este contiene los parÃ¡metros para correr el suricata, las partes principales de este archivo estÃ¡n en la configuraciÃ³n de la red, en donde se identifica la red local y la red externa, y las reglas que se quieren aplicar.


Archivo plugin para OSSIM.

El archivo de plugin para OSSIM es el archivo llamado SuricataIoT.cfg, este archivo contiene las especificaciones de la expresiÃ³n regular que permite al OSSIM entender los eventos generados por esta herramienta.



						INSTALACIÃ“N DE OPENVAS

Para la instalación del OpenVas deberemos realizar los siguientes paso uno a uno.

1. apt-get update
2. apt-get dist-upgrade
3. apt-get install openvas = Este paso podría tomar bastante tiempo.
4. openvas-setup = Retornada la creación del usuario “admin” con    su correspondiente clave.
5. netstat -antp = verificar el servicio
6. openvas-start
7. openvas-check-setup = Retornara un mensaje “OpenVas Installation OK”.
8. Abrir esta dirección url https://127.0.0.1:9392 = Ingresar el openvas con el usuario “admin” anteriormente mencionado y cambiar la clave.

Para correr Codigo OMP4-OpenVas

Utilizando contrab creamos un task en Linux para ejecutar el código de la siguiente forma "contrab -e"
Luego escirbimos el comando estableciento cuando y a que hora debera ejecturalo en este sera todos los dias a las 3 de la mañana con el siguiente codigo "0 3 * * * "path del codigo" java -jar OPM4-OpenVas.jar"

Archivo Plugin para OSSIM
El archivo de plugin para OSSIM es el archivo llamado openVasPlugin.cfg, este archivo contiene las especificaciones de la expresiÃ³n regular que permite al OSSIM entender los eventos generados por esta herramienta eviados por el codigo OMP4-OpenVas.






						DIRECTIVAS DE CORRELACIÃ“N

La primera directiva trata de tener dos eventos, unos de openvas y otro de suricata, el primer evento es la vulnerabilidad(CVE-2012-5964,ST URN ServiceType Buffer Overflow) de la librerÃ­a libupnp que es vulnerable a un ataque de denegaciÃ³n de servicio por medio de un mensaje del protocolo ssdp en donde el campo de service type de ese mensaje tiene un valor muy grande, y el segundo evento trata de un evento de suricata en donde identificar trÃ¡fico malicioso de un mensaje ssdp hacia el dispositivo upnp en donde se evidencia ciertas palabras claves que dan como positivo el ataque de denegaciÃ³n de servicio del dispositivo. El SIEM como respuesta a estos eventos realizarÃ¡ una actualizaciÃ³n de la librerÃ­a libupnp.

Esta directiva se puede probar en un dispositivo IoT uPnP que tenga la librerÃ­a libupnp en su versiÃ³n 1.3.1, en donde el dispositivo centinela con ayuda de Openvas detecta el uso de esta librerÃ­a vulnerable, luego con el monitoreo constante de la red de la herramienta de Suricata IoT, se debe detectar trÃ¡fico malicioso, en donde un mensaje del protocolo SSPD (En un paquete UDP) con el campo de â€œServiceTypeâ€ presenta un tamaÃ±o muy grande. 

Una vez la herramienta OSSIM recibe los eventos generados por las herramientas Openasvas y Suricata IoT, este debido a su configuraciÃ³n de la directiva de correlaciÃ³n genera una respuesta ejecutado un script en el dispositivo centinela, que a su vez ejecuta un script que actualiza la librerÃ­a vulnerable del dispositivo que estÃ¡ siendo atacado.

La segunda directiva trata de la denegaciÃ³n de servicio de un dispositivo que tenga un servicio web disponible, basado en el uso de un servicio de Nginx. El modo de operar es el mismo que el anterior, primero se tiene la vulnerabilidad (CVE-2013-2028, Exploit Specific) del dispositivo que dice que la versiÃ³n del servicio Nginx es vulnerable a ataques de denegaciÃ³n de servicio, el segundo evento es la evidencia de trÃ¡fico malicioso que da a entender que se estÃ¡ explotando la vulnerabilidad ya mencionada mediante una peticiÃ³n al dispositivo con unos campos especÃ­ficos. El SIEM como respuestas a estos eventos se genera la instalaciÃ³n de nginx.

La segunda directiva se prueba de tal forma que un dispositivo IoT use la librerÃ­a Ngix en su versiÃ³n 1.3.9 hasta la versiÃ³n 1.4.0, lo cual la herramienta de Openvas detecta el uso de esta librerÃ­a vulnerable, luego con el monitoreo constante de la red con la herramienta de Suricata IoT, se detecta una peticiÃ³n HTTP en donde el paquete tiene como encabezado â€œTransfer-Encoding: chunkedâ€.

Una vez la herramienta OSSIM recibe los eventos generados por las herramientas Openasvas y Suricata IoT, este debido a su configuraciÃ³n de la directiva de correlaciÃ³n genera una respuesta ejecutado un script en el dispositivo centinela, que a su vez ejecuta un script que actualiza la librerÃ­a vulnerable del dispositivo que estÃ¡ siendo atacado.


La tercera directiva de correlaciÃ³n se basa en  una vulnerabilidad(CVE: CVE-2017-13077) sobre el protocolo WPA(Acceso protegido Wi-Fi) analizada por  OpenVas (la cual se encuentra en el sistema operativo DEBIAN  con  versiÃ³n 2.3-1) y la alerta de kismet llamada BCASTDISCON la cual es lanzada cuando detecta que se estÃ¡ produciendo un ataque de desasociaciÃ³n de un cliente de la red generando una denegaciÃ³n de servicio.La relaciÃ³n que existe entre una y otras, es que la vulnerabilidad encontrada en WPA, es explotada por medio de un ataque de desasociaciÃ³n (desasocia a los clientes de red del protocolo WPA) de un cliente o varios que se encuentren en la  red y este es detectado por kismet, generando un evento en el SIEM e inmediatamente activa esta directiva y genera como respuesta el reinicio del sistema con el de desconectar al atacante del punto de acceso y envÃ­a un correo dueÃ±o del sistema para informarle de la situaciÃ³n.

Para probar esta directiva, se tiene que primero se identifica la vulnerabilidad en un dispositivo IoT relacionada a el protocolo WPA y WPS2, con ayuda de la herramienta de Openvas se detecta esta vulnerabilidad del dispositivo, para luego dejar que Kismet detecte un ataque de desasociacion del dispositivo de la red.

Para el caso de Kismet, esta herramienta identifica este ataque con la alerta llamada â€œBCASTDISCONâ€, que nos indica que se estÃ¡ realizando un ataque que aprovecha la vulnerabilidad de dispositivo representada en el CVE-2017-13077.

Una vez la herramienta OSSIM recibe los eventos generados por las herramientas Openasvas y Suricata IoT, este debido a su configuraciÃ³n de la directiva de correlaciÃ³n genera una respuesta ejecutado un script en el dispositivo centinela, que a su vez ejecuta un script que reinicia el dispositivo atacado.
