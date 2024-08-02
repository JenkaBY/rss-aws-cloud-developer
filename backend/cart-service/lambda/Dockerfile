FROM node:20-alpine AS build

# Create and pwd a working directory
WORKDIR /app

# Copy application dependency manifests to the container image.
# A wildcard is used to ensure copying both package.json AND package-lock.json (when available).
# Copying this first prevents re-running npm install on every code change.
COPY --chown=node:node package*.json .

# Install app dependencies
RUN npm install
# optimize build app. Some modules read this variable to optimise build
ENV NODE_ENV=production
# copy source code and config files. The files and directories listed in the .dockerignore file won't be copied
COPY --chown=node:node . .
# run build. the build-webpack is a command to bundle everithing in a single main.js file
RUN npm run build-webpack

# Swich to node user by security reason
USER node

# the second stage - runtime. It's used to build runtime image which will run our app.
FROM node:20-alpine AS runtime
#Create and pwd app directory
WORKDIR /app
# copy a bundled file from the build image to the runtime image
COPY --chown=node:node --from=build ./app/dist/main.js ./

# Tells developer about port used by app
EXPOSE 4000
# Swich to node user by security reason.
USER node

CMD [ "node", "main.js" ]