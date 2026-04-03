# Use Node.js 20 LTS - avoids Docker Hub rate limits; node:22 can hit toomanyrequests
FROM node:20-alpine

WORKDIR /app

# Copy package.json and package-lock.json
COPY package*.json ./

# Install dependencies
RUN npm install

# Copy the rest of the application code
COPY . .

# Expose the port Vite runs on
EXPOSE 5173

# Run the dev server with --host to expose it to the network
CMD ["npm", "run", "dev", "--", "--host"]
