## All the files for learning K8S with Java

* dockerhub customer: https://hub.docker.com/repository/docker/enoshima/springboot-k8s-customer/general
* dockerhub order: https://hub.docker.com/repository/docker/enoshima/springboot-k8s-order/general

* Command to run customer container with access to another service:
  * I commented **order** service host and port to force provision of params via -D. Plus, it helped me to catch
    errors/whether container is OK during K8S deploy phase
`docker run -e JAVA_OPTS="-Dorder.msa_host=host.docker.internal -Dorder.msa_port=8090" -p 8091:8091 springboot-k8s-customer:latest`
  * to make port-forward between deploy and svc: `kubectl port-forward deployment/customer-java-deploy 8091:8091`
* Recall that for using LoadBalancer svc we need to issue `minikube tunnel`