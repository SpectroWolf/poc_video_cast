📺 PocTransmissao – Google Cast (Online, Local e YouTube)

Este projeto demonstra como transmitir vídeos para dispositivos compatíveis com Google Cast (Chromecast, Android TV, Smart TV com Cast embutido).

Ele cobre três cenários principais:

Vídeos online (URLs diretas, ex.: MP4).

Vídeos offline (arquivos locais) – usando servidor HTTP interno.

Vídeos do YouTube – via SDK oficial ou fallback pelo app do YouTube.

🚀 Bibliotecas utilizadas
1. Google Cast SDK
implementation "com.google.android.gms:play-services-cast-framework:21.5.0"


Responsável por:

Descobrir dispositivos Cast na rede.

Gerenciar sessões de transmissão (CastContext, CastSession).

Enviar mídia (RemoteMediaClient) para o Chromecast.

Descrever conteúdo com MediaInfo e MediaLoadRequestData.

2. NanoHTTPD
implementation "org.nanohttpd:nanohttpd:2.3.1"


Responsável por:

Criar um servidor HTTP interno no dispositivo.

Necessário porque o Chromecast não consegue acessar arquivos locais diretamente (file://, content://).

Converte o arquivo local em uma URL acessível pela TV (ex.: http://192.168.1.100:8080/video.mp4).

3. Activity Result API
implementation "androidx.activity:activity-ktx:<versão>"


Responsável por:

Selecionar arquivos de vídeo no dispositivo.

Uso de ActivityResultContracts.GetContent() para abrir o seletor de mídia e obter um Uri.

4. YouTube Cast SDK (opcional)
implementation "com.google.android.gms:play-services-cast:21.5.0"
implementation "com.google.android.gms:play-services-cast-framework:21.5.0"


Responsável por:

Permitir transmitir vídeos do YouTube diretamente para a TV.

Utiliza o YouTube Player Receiver em vez do DEFAULT_MEDIA_RECEIVER_APPLICATION_ID.

⚠️ Alternativa: abrir o vídeo diretamente no app do YouTube via Intent (vnd.youtube:<videoId>), aproveitando o botão de Cast nativo do próprio app.

🔑 Requisitos

Android 7.0+ (SDK 24+).

Dispositivo e Chromecast devem estar na mesma rede Wi-Fi para transmissão de arquivos locais.

Permissões de acesso a arquivos (em Android 13+ usar o Storage Access Framework).
