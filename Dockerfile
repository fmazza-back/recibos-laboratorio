# Usamos una versión ligera de Node
FROM node:20-alpine

# Instalamos Java (Requisito estricto de Firebase Emulators)
RUN apk add --no-cache openjdk17-jre

# Instalamos la CLI de Firebase
RUN npm install -g firebase-tools

# Directorio de trabajo
WORKDIR /app

# Exponemos los puertos: UI (4000), Firestore (8080), Storage (9199)
EXPOSE 4000 8080 9199

# El comando que levanta el emulador escuchando en todas las interfaces de red
CMD ["firebase", "emulators:start", "--project", "demo-firmas", "--host", "0.0.0.0"]