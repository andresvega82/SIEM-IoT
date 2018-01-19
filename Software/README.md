INSTALACIÓN DE KISMET
                                                  
 Para la instalar kismet en Linux solo es escribir los siguientes comandos en la terminal:
-	Sudo apt-get update
-	Sudo apt-get Install kismet
Como el IDS Kismet fue instalado en una Raspberry Pi 3, es importarte saber que la tarjeta de red de este dispositivo no se deja cambiar a modo monitor, por lo tanto fue necesario implementarle la siguiente antena USB:

![imagen 1](https://github.com/andresvega82/SIEM-IoT/blob/master/Software/Kismet/1.jpg)
 
Configuración de la antena: Para cambiar la tarjeta a modo monitor se escriben los siguientes comandos en la consola:
-  sudo ifconfig wlan0 down
-  sudo iwconfig wlan0 mode monitor
-  sudo ifconfig wlan0 up
*La interface es a la que estaría conectada la antena, para mirar escriba el comando: ifconfig

Configuraciónn de kismet:
Vamos al archivo de configuración de kismet  kismet.conf que generalmente se encuentra en la ruta /etc/kismet/kimet.conf y la abrimos con el siguiente comando:
-	nano kismet.conf
Añadimos al final del archivo “ncsource = wlan0” sin las “”, para que kismet reconozca por de interface se va hacer el escaneo. 
Corriendo Kismet:
Para correr kismet ese escribe el comando:
-	kismet
Se abrirá una ventana de kismet: 
 
![Imagen 2](https://github.com/andresvega82/SIEM-IoT/blob/master/Software/Kismet/2.png)

Si queremos ver la información por la consola de kismet le damos click en []Show Console  
Le damos click en Start. 
Y kismet empieza a monitorer las redes WIFI. 
 
![Imagen 3](https://github.com/andresvega82/SIEM-IoT/blob/master/Software/Kismet/3.png)


#CONFIGURACIÓN KISMET EN OSSIM

Para cuando Kismet genere la alerta generalmente la guarda en la siguiente ruta etc/kismet y la envia al OSSIM y para esto se debe tener corriendo el programa que está en el siguiente Github https://github.com/andresvega82/SIEM-IoT/tree/master/Software/Kismet
en el archivo Syslogkismet.zip,  el cual envía el syslog al OSSIM para que este pueda recibir el evento. 
En la siguiente imagen podemos ver en color amarillo el código donde se envía el syslog:  
  
 
![Imagen 4](https://github.com/andresvega82/SIEM-IoT/blob/master/Software/Kismet/4.png)

Este programa lee los nuevos archivos que se van creando en la carpeta  ect/kismet y los envía al ossim.

La forma en que llega el evento al OSSIM es la siguiente:

![EventoKismet](https://github.com/andresvega82/SIEM-IoT/blob/master/Software/Kismet/enventoKismet.PNG)

**Es necesario incluir las clases:

 ![Imagen 4.1](https://github.com/andresvega82/SIEM-IoT/blob/master/Software/Kismet/4.1.png)
 
**Para correr el programa se genera un .jar
** y se ejecuta con el conmando java -jar <el nombre del archivo >.Jar.

Ya con lo anterior se envía el syslog al OSSIM , pero para indicarles que campos del syslog queremos que se vea en el evento, es necesario agregar en la plataforma de OSSIM le plugin kismetIoTPlugin.cfg, el cual se encuentra en el Github mencionado anteriormente, el cual contiene la expresión regular: 
 
![Imagen 5](https://github.com/andresvega82/SIEM-IoT/blob/master/Software/Kismet/5.png)

Continuación de la expresión regular
 
![Imagen 6](https://github.com/andresvega82/SIEM-IoT/blob/master/Software/Kismet/6.png)

La cual hará que el OSSIM reciba y muestre el evento de la siguiente forma:

![Imagen 7](https://github.com/andresvega82/SIEM-IoT/blob/master/Software/Kismet/7.png)

**PROBAR KISMET**

En este escenario vamos a generar la alerte BCASTDISCON la cual se dispara cuando detecta que hay un ataque de desasociación de la red de un cliente o de varios, causando una posible denegación de servicio. 
Debemos tener el Kismet corriendo: 
Comando : Kismet 
Para generar el ataque vamos a utilizar la herramienta aireplay-ng, en nuestro caso vamos a desconectar a todos los clientes conectados a la red: 
Escribimos el siguiente comando: 
-	aireplay-ng –deauth 0 –a < BSSID> wlan1

*el 0(cero) indica que va a realizar el ataque indefinidamente, si queremos que solo se realice 1 vez, cambiamos el 0 por 1.
* wlan1 es la interface de red.
* El BSSID es el punto de acceso por el cual se va a realizar el ataque.

 ![Imagen 8](https://github.com/andresvega82/SIEM-IoT/blob/master/Software/Kismet/8.png)

Cualquiera que se encuentre en la red para visualizar mejor estas BSSID podemos utilizar el comando: airodump-ng wlan1
, utilizando la interface de red que tengamos en el momento.

Luego le damos Enter y se empieza el ataque, se demora un poco ya que tiene que coincidir el canal(CH) del BSSID con el de la interface de red(wlan1). 
 
![Imagen 9](https://github.com/andresvega82/SIEM-IoT/blob/master/Software/Kismet/9.png)

En la imagen anterior ponemos ver que empieza a enviar paquetes de desasociación por el punto que acceso que definimos y de este modo generar una denegación de servicio. 

Kismet detecta el ataque:
 
![Imagen 10](https://github.com/andresvega82/SIEM-IoT/blob/master/Software/Kismet/10.png)

Informado que tipo de ataque es, por donde se está generando el ataque y cual la consecuencia de este. 
Cuando kismet recibe la alerta esta es reportada al OSSIM y el OSSIM por medio del siguiente script, realiza una acción: 
 
![Imagen 11](https://github.com/andresvega82/SIEM-IoT/blob/master/Software/Kismet/11.png)

En nuestro caso lo que hace el reiniciar el equipo cerrando el punto de acceso del atacante (se debe tener en cuenta que para este paso se deben tener permisos de administrador). En el OSSIM se programa de la siguiente manera:

![Imagen 12](https://github.com/andresvega82/SIEM-IoT/blob/master/Software/Kismet/12.png)

 El COMMAND:* se indica el scripts que se va a utilizar. 
En TO* se puede poner un correo para informar al administrador de que ocurrió el evento. 

						INSTALACIÓN DE SURICATA IOT
												
1.	Lista que librerías para instalar sobre SO:
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
    
    
![Imagen 33](https://github.com/andresvega82/SIEM-IoT/blob/master/Software/Suricata/01.png)


2.	Descargar comprimido de Suricata:
	git clone https://github.com/decanio/suricata-IoT.git

3.	Entrar a la carpeta de suricata:
	cd suricata-3.1

4.	Instalar:
	./autogen.sh
	./configure && make && make install-full
	ldconfig

5.	Copiar archivos de configuración:
	mkdir /var/log/suricata
	mkdir /etc/suricata
	mkdir /etc/suricata/rules
	cp classification.config /etc/suricata
	cp reference.config /etc/suricata
	cp suricata.yaml /etc/suricata

6.	Habilitar el envió de alertas al servicio syslog para enviar alertas a OSSIM, editar archivo “suricata.yaml”:

![Imagen 60](https://github.com/andresvega82/SIEM-IoT/blob/master/Software/Suricata/11.png) 

7.	Incluir el envió de alertas de Suricata que tiene como prefijo “local5” por el servicio Syslog, incluir la siguiente línea en el archivo /etc/rsyslog.conf:
*.local5@ip_servidor_OSSIM:puerto

8.	Reiniciar servicio rsyslog:
sudo service rsyslog restart

8.	Bajar reglas (por defecto se bajan por Oinkmaster):
	apt-get install oinkmaster
	editar el archivo oinkmaster.conf: /etc/oinkmaster.conf
	adicionar línea: 
	url = http://rules.emergingthreats.net/open/suricata/emerging.rules.tar.gz
	comando: oinkmaster -C /etc/oinkmaster.conf -o /etc/suricata/rules

7.	Correr Suricata:
	suricata -c /etc/suricata/suricata.yaml -i eth0 --init-errors-fatal
	
	
    

![Imagen 34](https://github.com/andresvega82/SIEM-IoT/blob/master/Software/Suricata/02.png)

#### Archivo de configuración suricata.yaml

El archivo de configuración de Suricata IoT es el archivo llamado suricata.yaml, este contiene los parámetros para correr el suricata, las partes principales de este archivo están en la configuración de la red, en donde se identifica la red local y la red externa, y las reglas que se quieren aplicar.

![Imagen 35](https://github.com/andresvega82/SIEM-IoT/blob/master/Software/Suricata/03.png)

En esta parte del archivo de configuración suricata.yaml sirve para configurar las propiedades de la red en donde está el dispositivo centinela, en tal caso, se define la variable de “HOME_NET”, en donde se coloca el identificador de red y la máscara, como lo muestra la imagen, de igual modo, se puede configurar otro tipo de variable en donde se encuentran algunos servicios de red como el servidor HTTP o el DNS.


![Imagen 36](https://github.com/andresvega82/SIEM-IoT/blob/master/Software/Suricata/04.png)

En este punto del archivo de configuración suricata.yaml permite escoger las reglas que van a ser revisadas por el software Suricata, en este punto se debe ver que cada archivo de reglas tiene un esquema de nombre_del_archivo.rules, en donde el nombre describe el paquete de reglas de se evalúan. Por otro lado, también se configura la ubicación en donde se encuentran las reglas, esta configuración se ve en la asignación de la variable “default-rule-path”.

![Imagen 37](https://github.com/andresvega82/SIEM-IoT/blob/master/Software/Suricata/05.png)

Por último, en esta parte del archivo de configuración suricata.yaml permite configurar las diferentes salidas de reportes de alertas o avisos que emite el sistema, como muestra la imagen permite configurar un archivo de estadísticas, al colocar el campo de “enables” en “yes” se toma que se quiere un archivo de reporte de estadísticas cada cierto tiempo definido en la variable “Interval”. Todos estos archivos de reportes se pueden consultar en la ruta colocada en la variable de “default-log-dir”.

#### Archivo plugin para OSSIM.

El archivo de plugin para OSSIM es el archivo llamado SuricataIoT.cfg, este archivo contiene las especificaciones de la expresión regular que permite al OSSIM entender los eventos generados por esta herramienta.

![Imagen 38](https://github.com/andresvega82/SIEM-IoT/blob/master/Software/Suricata/07.png)

Con este archivo de plugin el sistema OSSIM reconoce los mensajes del protocolo syslog que tienen específicamente el formato determinado por la expresión regular ya mencionada, de tal forma que nos permite obtener información importante para determinar el tipo de tráfico malicioso, para ello OSSIM permite declarar variables tomadas de la expresión regular las cuales son las siguientes:
•	src_ip, esta variable contiene la ip fuente del tráfico identificado como malicioso.
•	src_port, esta variable contiene el puerto fuente del tráfico malicioso.
•	dst_ip, esta variable contiene la ip de destino del tráfico malicioso.
•	userdata_1, esta variable contiene el puerto destino del tráfico malicioso.
•	userdata_2, esta variable contiene el número de identificación de la regla Suricata que se envía.
•	priority, esta variable contiene el número de prioridad definida en la regla Suricata del trafico malicioso.

#### Probar Suricata

1.	Para probar Suricata corremos el comando en la terminal:
suricata -c /etc/suricata/suricata.yaml -i eth0 --init-errors-fatal

2.	Una vez comience a correr el mismo sistema empieza a examinar los paquetes de la red en busca que coincida con alguna regla.

3.	De haber un paquete que coincida con alguna regla de Suricata y se genere una alerta Suricata, el sistema automáticamente lo enviará al servidor OSSIM y lo entenderá como un mensaje de alerta Suricata. Lo anterior se logra gracias a que se configuro que toda alerta sea dirigida al servicio rsyslog y que se envíe al sistema OSSIM como un mensaje Syslog (Ver proceso de instalación de suricata en los pasos 6,7 y 8).

![Imagen 45](https://github.com/andresvega82/SIEM-IoT/blob/master/Software/Suricata/06.png)

						INSTALACIÓN DE OPENVAS

Para la instalación del OpenVas deberemos realizar los siguientes pasos, uno a uno.

1. apt-get update
2. apt-get dist-upgrade
3. apt-get install openvas = Este paso podría tomar bastante tiempo.
4. openvas-setup = Retornada la creación del usuario admin con su correspondiente clave.
5. netstat -antp = verificar el servicio
6. openvas-start
7. openvas-check-setup = Retornara un mensaje "OpenVas Installation OK".
8. Abrir esta dirección url para comprobar que el servicio este disponible https://127.0.0.1:9392 = Ingresar con el usuario admin anteriormente mencionado y cambiar la clave.

![Imagen 16](https://github.com/andresvega82/SIEM-IoT/blob/master/Software/Openvas/openvas-09.png)

**Para correr Codigo OMP4-OpenVas**

Este codigo  fue creado con el fin de enviar todos los resultados hacia OSSIM de cada analisis realizado por OpenVAS utilizando el API de la herramienta llamada OpenVAS Management Protocol (**OMP**). 

![Imagen 44](https://github.com/andresvega82/SIEM-IoT/blob/master/Software/Openvas/02.png)

1. Utilizando contrab creamos un task en Linux para ejecutar el código de la siguiente forma "contrab -e"
2. Luego escirbimos el comando estableciento cuando y a que hora debera ejecturalo en este sera todos los dias a las 3 de la mañana con el siguiente codigo "0 3 * * * "path del codigo" java -jar OPM4-OpenVas.jar"

**Archivo Plugin para OSSIM**

El archivo de plugin para OSSIM es el archivo llamado openVasPlugin.cfg, este archivo contiene las especificaciones de la expresión regular que permite al OSSIM entender los eventos generados por esta herramienta eviados por el codigo OMP4-OpenVas.

**La expresión regular para OSSIM es:**

"([\s\S]+)(OpenVas: )(\|)(?P<vul_id>[\s\S]+)(\|)(?P<ip_address>[\s\S]+)(\|)(?P<severity>[\s\S]+)(\|)(?P<cve>[\s\S]+)"

La cual inicia con un indicador "OpenVas: " que noos indica que es un mensaje de la herramienta, luego de esto vienen 4 campos distintos separados por "|" el primer campo es el id de la vulnerabilidad, segundo campo el ip de la fuente, tercer campo severidad de la vulnerabilidad y en el ultimo campo el cve asociado con la vulnerabilidad.

**Codigo OpenVasOMP:**

El codigo del OpenVasOMP cuenta con diversas clases java para realizar peticiones que el protocolo OMP nos permite para interactuar con OpenVas en este caso estamos utilizando consultas xml desde el codigo para extrar los resultados y ser enviados al OSSIM

**Diagrama de clases OpenVasOMP**

![Imagen 70](https://github.com/andresvega82/SIEM-IoT/blob/master/Software/Openvas/DiagramaDeClases.PNG)

La clase principal es Client la encargada de realizar todo el proceso, lo primero que hace es utilizar un método de si misma “getResults” el cual crea una instancia de “GetResults” en donde se identifica el comando necesario para obtener los resultados directamente del OpenVas, cuando genera el comando este retorna un código xml el cual es usado para crear una nueva instancia de la clase “GetResultsResponse”.
Una vez que tenemos esta instancia creada es retornada a Client y se procede a utilizar el método “getSysloginfo” quien se encarga de separar cada resultado identificado en el xml y a su vez utiliza el método “getSyslogAtributes” quien se encarga de obtener los atributos deseados de cada resultado respectivamente y se organiza en ArrayList para ser enviado al OSSIM. 
Cuando Client ya obtiene el ArrayList de cada resultado con sus respectivos atributos, procede a crear un mensaje syslog por resultado del ArrayList obtenido con la clase “UdpSyslogMessageSender” y a través del método “sendMessage” el cual enviara el mensaje syslog a la dirección establecida desde la clase Client. 


**Prueba OpenVas:**

Una vez que hemos iniciado sesion como administradores (como se dice en el manual de instalación) nos dirigiremos a targets y alli creamos uno nuevo con la ip que deseemos escanear mientras se encuntre en la misma red que el OpenVas, seguido de esto en task, crearemos un nuevo task apuntando al target anteriormente creado y le damos start, si funciona correctamente el debera iniciar una carga porcentual que tardara un tiempo dependiendo que tan invasivo sera el escaneo, al final de esta carga podremos ver todas las vulnerabilidades encontradas en el target, clasificadas en 4 severidades diferentes (Informativas, bajas, media , alta)











						DIRECTIVAS DE CORRELACIÓN
#### Directiva de Correlación No.1

La primera directiva trata de tener dos eventos, unos de openvas y otro de suricata, el primer evento es la vulnerabilidad(CVE-2012-5964,ST URN ServiceType Buffer Overflow) de la librería libupnp que es vulnerable a un ataque de denegación de servicio por medio de un mensaje del protocolo ssdp en donde el campo de service type de ese mensaje tiene un valor muy grande, y el segundo evento trata de un evento de suricata en donde identificar tráfico malicioso de un mensaje ssdp hacia el dispositivo upnp en donde se evidencia ciertas palabras claves que dan como positivo el ataque de denegación de servicio del dispositivo. El SIEM como respuesta a estos eventos realizará una actualización de la librería libupnp.

Esta directiva se puede probar en un dispositivo IoT uPnP que tenga la librería libupnp en su versión 1.3.1, en donde el dispositivo centinela con ayuda de Openvas detecta el uso de esta librería vulnerable, luego con el monitoreo constante de la red de la herramienta de Suricata IoT, se debe detectar tráfico malicioso, en donde un mensaje del protocolo SSPD (En un paquete UDP) con el campo de “ServiceType” presenta un tamaño muy grande. 

Una vez la herramienta OSSIM recibe los eventos generados por las herramientas Openasvas y Suricata IoT, este debido a su configuración de la directiva de correlación genera una respuesta ejecutado un script en el dispositivo centinela, que a su vez ejecuta un script que actualiza la librería vulnerable del dispositivo que está siendo atacado.

El orden de la ejecución de esta directiva queda de esta manera:

1.	El dispositivo centinela detecta la vulnerabilidad asociada con el código de CVE-2012-5964.

![Imagen 46](https://github.com/andresvega82/SIEM-IoT/blob/master/Software/Suricata/09.PNG)

2.	El dispositivo centinela detecta tráfico malicioso y lo envía a la plataforma de OSSIM.

![Imagen 39](https://github.com/andresvega82/SIEM-IoT/blob/master/Software/Suricata/06.png)

3.	Gracias a la configuración de OSSIM se genera la correlación cruzada de eventos, esta consiste en tener diferentes fuentes de eventos de seguridad reportando que permiten inferir ataques de seguridad, en el caso de esta directiva de genera un evento de Openvas y otro evento de Suricata, lo cual al tener eventos de estas dos fuentes se activa la directiva de correlación cruzada.Cabe resaltar que para que se active esta directiva de correlación los eventos que llegan deben ser del mismo tipo, tanto la vulnerabilidad específica y el ataque.

![Imagen 47](https://github.com/andresvega82/SIEM-IoT/blob/master/Software/Suricata/10.png)

4.	La plataforma OSSIM detecta el ataque y genera una respuesta de contingencia al ataque, para este caso se actualiza la librería libupnp del dispositivo atacado.

![Imagen 40](https://github.com/andresvega82/SIEM-IoT/blob/master/Software/Suricata/08.png)

Para el caso de esta respuesta activa, cabe resaltar que se debe tener permisos de administrador del dispositivo que se va a realizar la operación de actualización en el dispositivo centinela para que esta respuesta funcione.

#### Directiva de Correlación No.2
La segunda directiva trata de la denegación de servicio de un dispositivo que tenga un servicio web disponible, basado en el uso de un servicio de Nginx. El modo de operar es el mismo que el anterior, primero se tiene la vulnerabilidad (CVE-2013-2028, Exploit Specific) del dispositivo que dice que la versión del servicio Nginx es vulnerable a ataques de denegación de servicio, el segundo evento es la evidencia de tráfico malicioso que da a entender que se está explotando la vulnerabilidad ya mencionada mediante una petición al dispositivo con unos campos específicos. El SIEM como respuestas a estos eventos se genera la instalación de nginx.

La segunda directiva se prueba de tal forma que un dispositivo IoT use la librería Ngix en su versión 1.3.9 hasta la versión 1.4.0, lo cual la herramienta de Openvas detecta el uso de esta librería vulnerable, luego con el monitoreo constante de la red con la herramienta de Suricata IoT, se detecta una petición HTTP en donde el paquete tiene como encabezado “Transfer-Encoding: chunked”.

Una vez la herramienta OSSIM recibe los eventos generados por las herramientas Openasvas y Suricata IoT, este debido a su configuración de la directiva de correlación genera una respuesta ejecutado un script en el dispositivo centinela, que a su vez ejecuta un script que actualiza la librería vulnerable del dispositivo que está siendo atacado.

El orden de la ejecución de esta directiva queda de esta manera:

1.	El dispositivo centinela detecta la vulnerabilidad asociada con el código de CVE-2012-5964.

![Imagen 48](https://github.com/andresvega82/SIEM-IoT/blob/master/Software/Openvas/04.PNG)

2.	El dispositivo centinela detecta tráfico malicioso y lo envía a la plataforma de OSSIM.

![Imagen 49](https://github.com/andresvega82/SIEM-IoT/blob/master/Software/Openvas/03.jpeg)

3.	Gracias a la configuración de OSSIM se genera la correlación cruzada de eventos, esta consiste en tener diferentes fuentes de eventos de seguridad reportando que permiten inferir ataques de seguridad, en el caso de esta directiva de genera un evento de Openvas y otro evento de Suricata, lo cual al tener eventos de estas dos fuentes se activa la directiva de correlación cruzada.Cabe resaltar que para que se active esta directiva de correlación los eventos que llegan deben ser del mismo tipo, tanto la vulnerabilidad específica y el ataque.

![Imagen 50](https://github.com/andresvega82/SIEM-IoT/blob/master/Software/Openvas/05.png)

4.	La plataforma OSSIM detecta el ataque y genera una respuesta de contingencia al ataque, para este caso se actualiza la librería Nginx del dispositivo atacado.

![Imagen 41](https://github.com/andresvega82/SIEM-IoT/blob/master/Software/Openvas/01.png)

Para el caso de esta respuesta activa, cabe resaltar que se debe tener permisos de administrador del dispositivo que se va a realizar la operación de actualización en el dispositivo centinela para que esta respuesta funcione.

#### Directiva de Correlación No.3

La tercera directiva de correlación se basa en  una vulnerabilidad(CVE: CVE-2017-13077) sobre el protocolo WPA(Acceso protegido Wi-Fi) analizada por  OpenVas (la cual se encuentra en el sistema operativo DEBIAN  con  versión 2.3-1) y la alerta de kismet llamada BCASTDISCON la cual es lanzada cuando detecta que se está produciendo un ataque de desasociación de un cliente de la red generando una denegación de servicio.La relación que existe entre una y otras, es que la vulnerabilidad encontrada en WPA, es explotada por medio de un ataque de desasociación (desasocia a los clientes de red del protocolo WPA) de un cliente o varios que se encuentren en la  red y este es detectado por kismet, generando un evento en el SIEM e inmediatamente activa esta directiva y genera como respuesta el reinicio del sistema con el de desconectar al atacante del punto de acceso y envía un correo dueño del sistema para informarle de la situación.

Para probar esta directiva, se tiene que primero se identifica la vulnerabilidad en un dispositivo IoT relacionada a el protocolo WPA y WPS2, con ayuda de la herramienta de Openvas se detecta esta vulnerabilidad del dispositivo, para luego dejar que Kismet detecte un ataque de desasociacion del dispositivo de la red.

Para el caso de Kismet, esta herramienta identifica este ataque con la alerta llamada “BCASTDISCON”, que nos indica que se está realizando un ataque que aprovecha la vulnerabilidad de dispositivo representada en el CVE-2017-13077.

Una vez la herramienta OSSIM recibe los eventos generados por las herramientas Openasvas y Suricata IoT, este debido a su configuración de la directiva de correlación genera una respuesta ejecutado un script en el dispositivo centinela, que a su vez ejecuta un script que reinicia el dispositivo atacado.

El orden de la ejecución de esta directiva queda de esta manera:

1.	En este escenario vamos a generar la alerte BCASTDISCON la cual se dispara cuando detecta que hay un ataque de desasociación de la red de un cliente o de varios, causando una posible denegación de servicio. 
Debemos tener el Kismet corriendo: 
Comando : Kismet

2.	Para generar el ataque vamos a utilizar la herramienta aireplay-ng, en nuestro caso vamos a desconectar a todos los clientes conectados a la red: 
Escribimos el siguiente comando: - aireplay-ng –deauth 0 –a < BSSID> wlan1

 ![Imagen 42](https://github.com/andresvega82/SIEM-IoT/blob/master/Software/Kismet/8.png)

3.	Kismet detecta el ataque sobre el dispositivo.

 ![Imagen 43](https://github.com/andresvega82/SIEM-IoT/blob/master/Software/Kismet/10.png)

4.	OSSIM detecta el ataque sobre el dispositivo gracias al evento enviado desde kismet y la vulnerabilidad hallada por OpenVas sobre el Protocolo WAP:
    Las siguientes imágenes nuestran como llegan los eventos de esta regla de correlación al OSSIM

![Imagen 51](https://github.com/andresvega82/SIEM-IoT/blob/master/Software/Kismet/14.PNG)
Donde se muestra: el tipo de alerta que genero kismet, el punto de acceso del ataque y el tipo de ataque que se realizó.
![Imagen 52](https://github.com/andresvega82/SIEM-IoT/blob/master/Software/Kismet/13.PNG)
En este evento se muestra la vulnerabilidad la cual está explotando                                                     el ataque que se está generando.

5.	Gracias a la configuración de OSSIM se genera la correlación cruzada de eventos, esta consiste en tener diferentes fuentes de eventos de seguridad reportando que permiten inferir ataques de seguridad, en el caso de esta directiva de genera un evento de Openvas y otro evento de Kismet, lo cual al tener eventos de estas dos fuentes se activa la directiva de correlación cruzada. Cabe resaltar que para que se active esta directiva de correlación los eventos que llegan deben ser del mismo tipo, tanto la vulnerabilidad específica y el ataque

![Imagen 53](https://github.com/andresvega82/SIEM-IoT/blob/master/Software/Kismet/15.png)

6.	OSSIM detecta el ataque sobre el dispositivo y genera la respuesta activa, la cual es reiniciar el dispositivo IoT.

 ![Imagen 44](https://github.com/andresvega82/SIEM-IoT/blob/master/Software/Kismet/11.png)
 
 Para el caso de esta respuesta activa, cabe resaltar que se debe tener permisos de administrador del dispositivo que se va a realizar la operación de actualización en el dispositivo centinela para que esta respuesta funcione.