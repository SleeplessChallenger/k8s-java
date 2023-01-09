## All the files for learning K8S with Java

* dockerhub customer: https://hub.docker.com/repository/docker/enoshima/springboot-k8s-customer/general
* dockerhub order: https://hub.docker.com/repository/docker/enoshima/springboot-k8s-order/general

* Command to run customer container with access to another service:
`docker run -e JAVA_OPTS="-Dorder.msa_host=host.docker.internal -Dorder.msa_port=8090" -p 8091:8091 springboot-k8s-customer:latest`