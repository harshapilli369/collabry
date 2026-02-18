# Use Node.js 22
FROM node:22-alpine

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
