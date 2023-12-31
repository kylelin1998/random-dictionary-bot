```
docker build -t rdb .
docker restart rdb
docker logs --tail=100 rdb
docker stop rdb && docker rm rdb
```

```
docker run --name rdb --network=host -d --log-opt max-size=10MB --log-opt max-file=5 -v $(pwd):/app --restart=always rdb
```