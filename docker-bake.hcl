group "default" {
  targets = ["frontend", "backend"]
}

target "frontend" {
  context = "frontend"
  tags = ["fi-lo-games-frontend:latest"]
  platforms = ["linux/amd64", "linux/arm64"]
}

target "backend" {
  context = "backend/fi-lo-games"
  tags = ["fi-lo-games-backend:latest"]
  platforms = ["linux/amd64", "linux/arm64"]
}