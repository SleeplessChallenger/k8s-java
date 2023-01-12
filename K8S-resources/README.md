### Architecture:

1. Master and Worker nodes:
	- master node is a so-called brain
		- it runs all cluster's control plane services
	- worker node - does all the heavy lifting things 
2. Master node == **control plane**:
	- *API server (api)*
	- scheduler (sched)
	- cluster store (etcd)
	- controller manager (c-m)
	- cloud manager (c-c-m) - communicates with i.e. AWS etc

=> all the components above communicate via the **API Server**. Worker
nodes are outside of the **control plane**.

Each component inside **master node**:

1. API Server (communicate via `kubectl apply -f`
	1.1 Frontend to K8S Control Plane
	1.2 Internal and External communications go through this API
	1.3 Exposes API on port 443
	1.4 Authentication and Authorization checks

2. Cluster Store - **etcd**
	2.1 stores configuration and state of the entire cluster
	2.2 distributed key-value data store
	2.3 SSOT - it is our database
	    - `kubectl apply -f` with YAML will go through API Server to etcd

3. Scheduler:
	3.1 watches for new pods/workloads and assigns them to a node 
based on multiple scheduling factors: healthy, enough resources, port 
available, affinity and anti affinity rules

4. Controller Manager - daemon that manages the control loop. 
Controller of Controllers
	4.1 A bunch of controllers (i.e. Node Controller - checks the 
state of the node. If something with node - new is created)
	+ other controllers: ReplicaSet, Endpoint, Namespace, Service 
Accounts
	4.2 Each controller watches (watch loop = controller) the 
**API Server** for changes and compares with current state. If 
this change makes our system not match the **desired state**, the 
particular controller tries to match the desired state.

5. Cloud Controller Manager:
	5.1 Responsible to interact with underlying cloud provider.
		- `kubectl apply -f` with YAML will go through API Server to 
etcd and then to c-c-m (Cloud Controller Manager)
	It is responsible for load balancers, storage, instances

### Worker Node:

1. VM or Physical Machine which provides running environment
2. Inside node: pods

3 Main components of worker node:

 1. Kubelet
    - **Main Agent** that runs on every node
    - Receives Pod definitions from API server
    - Interacts with Container Runtime to run containers associated 
with the Pod
    - Reports Node and Pod state to master
 
 2. Container runtime
	- responsible for pulling images from container registries 
(like Docker Hub), starting them and stoping them
	- responsible for running containers and abstracts container 
management for Kubernetes
	- CRI - container runtime interface. Interface for 3rd party 
container runtime. ContainerD over Docker
	- https://earthly.dev/blog/containerd-vs-docker/
	- https://www.tutorialworks.com/difference-docker-containerd-runc-crio-oci/
 
 3. Kube Proxy 
    - runs on every node through **DaemonSets**
    - responsible for:
        - local cluster networking
        - each node gets own unique IP address
        - routing network traffic to load balanced services
    - I.e. if 2 pods want to communicate to each other, Kube Proxy 
does the magic. If outside network sends a request -> also Kube Proxy

Running K8S:
- by yourself: hard
- managed K8S:
	- EKS, GKE, AKS etc
	- in this case you focus only on **Worker Nodes**

Running Kube cluster locally:
- local cluster: minikube, kind, docker

###Commands:

* [kubectl cheat sheet](https://kubernetes.io/docs/reference/kubectl/cheatsheet/)

1. `kubectl` is a tool to manipulate
2. `kubectl run hello-world --image=name_of_the_image:here_tag` - create pod
3. `kubectl get nodes`; `kubectl get pods`

namespace - construct that logically allows to separate one cluster for sub-clusters:
	- [K8S docs](https://kubernetes.io/docs/concepts/overview/working-with-objects/namespaces/)

`kubectl get pods -A` - get pods from all namespaces

* to ssh into node (by default it will ssh in node with `default` namespace): `minikube ssh`

* to delete the whole cluster: `minikube delete`. To stop the cluster: `minikube stop`

* to start cluster with 2 nodes (1 master and 1 worker): `minikube start --nodes=2`
	- `minikube ip --node name_of_the_node` - it will show IP address of the desired node

* `minikube logs` - to get logs of master node. Also you can `minikube logs -f` to follow the logs
* `minikube logs --node='name_of_the_node'` - to see logs of the particular pod

### Pod commands

* pod is the smallest deployable unit in K8S. Containers are inside pods
	- *init container* is executed before *main container*. 
	Also there are *side containers* - support main container. I.e. proxy
* *volumes* - way to share data between containers
* containers communciate with each other inside pod via localhost (unique IP address)

* **NEVER** create pods on its own. Use *controllers* instead. Because pods are killed and started all the time. Hence if K8S doesn't know anything about them, it can't restart them
	- Smallest deployable unit: Docker - container, K8S - pod

* pod creation (and not only for **pods**, actually, for everything):
	- imperative command: `kuebctl run pod-name --image ...`
	- declarative configuration: define same command as above using config file (i.e. `.yaml`)

* to proxy from outer world to the pod (testing purposes way. Production way - services):
	- `kubectl run pod-name --image=image-name --port=port_num`
	- `kubectl port-forward pod/pod_name outer_port:pod_port`

* create pod via config (look at `pod.yml`): 
	- `kubectl apply -f config.yaml`
	- make port forwarding: `kubectl port-forward pod/hello-world 8080:80`

### Other commands

* `cat pod.yml | kubectl apply -f -` - another way to create pod from file
* `kubectl get pods -w` - to watch for pods
* `kubectl delete -f file.yml` - delete pod created from the particular config file
* `kubectl get pods -A` - to see all pods in all namespaces
* `kubectl get all` - give everything in default namespace + add `-A` to see all namespaces and everything within them
* `kubectl get pod -n kube-system` - to see pod in particular namespace (swap `pod` for `all` to see eveything, not only pods)
* `kubectl get namespaces` - just list all namespaces

+ in `.yml` we can have services, volumes etc

* `kubectl describe pod hello-world` - to see detailed info about particular pod. More concise: `kubectl get pod hello-world -o wide` (`-o == --output`)

+ `kubectl get pod pod_name -o yaml` - to present in yaml. Or even in `json` 

* see logs of the pod: `kubectl logs pod_name`

* look at the *Containers* section in `describe`:
	- `kubectl logs hello-world -c container_name`

* to enter in the running pod's container: 
	- `kubectl exec -it hello-world -- bash`
	- `kubectl exec -it hello-world -c hello-world -- bash`: to enter the exact container
	- without `-it` it will just show the info: `kubectl exec hello-world -c hello-world -- ls /`
	- to see the processes: `kubectl exec container_name -- ps aux`

* to make app inside pod being accessed from outer world we need to make port-forward: `kubectl port-forward hello-world 8084:80`. Or: `kubectl port-forward pod/hello-world 8083:80`
	- **IMPORTANT:** we can port-forward not only pods, but service, daemonSets etc + such a method is good only for testing purposes

* to list all resources: `kubectl api-resources`

### Deployments

* never deploy pods in production using: `kind: Pod` as they're ephemeral
* pods don't self-heal

=> manage pods through `Deployments`

* Perks:
	- manages release of new application (facilitates software deployments):
		- if new version is released, it is given new tag (i.e. v2). After eveything is good, v1 will be deprecated
	- zero-downtime deployments
	- creates ReplicaSet:
		- ReplicaSet: ensures desired number of pods is running

* Part of `yaml`:
```
  selector:
    matchLabels:
      app: hello-world
```
says that this deployment is for `template` below (look in yaml)

```
  selector:
    matchLabels:
      app: hello-world # these two have to match for deployment to work
  template:
    metadata:
      labels:
        app: hello-world # these two have to match for deployment to work
```

* `kubectl apply -f deployment.yaml`
* we have random number after pod name as we can have multiple replicas
* `kubectl describe deployment hello-world` - to observe deployment

* ReplicaSet: ensures desired number of pods always running.
	- [About controller manager](https://kubernetes.io/docs/reference/command-line-tools-reference/kube-controller-manager/)
	- *deployment* has ReplicaSet
	- it uses *control loops* (as RS a controller itself) to ensure the desired number of pods are always running. ReplicaSets implement *background control loop* to check that desired number of pods are always running on the cluster.
	- *RS* ensures that we have desired number of pods on the cluster. Not necessarily on one cluster.

* ReplicaSet is created with deployment
* `kubectl port-forward deployment/hello-world 8082:80` - to forwarding deployment.
	- **IMPORTANT:** this is good only for testing. In production: services

* *Rolling update:* make K8S to take care of rolling update for us when we have new version of our application.
	- deployment -> v2 RS -> v2 full of pods
	- after next version is up-and-running, K8S scales down old version
	- in `deployment.yaml` we provided new version of image.
	- K8S doesn't delete old RS for *rollback*

* to view changes of rollouts: `kubectl rollout history deployment deploy_name`
* to view the help: `kubectl rollout -h`
* `kubectl rollout status deployment hello-world` - to see the status of the rollout
* to make a rollout to prev version: `kubectl rollout undo deployment/hello-world`
* to hop to the particular revision: `kubectl rollout undo deployment/hello-world --to-revision=2`
* to view info about the particular revision: `kubectl rollout history deployment/hello-world --revision=2`
* to configure revision you can put in the `spec`: `revisionHistoryLimit`

### Deployment strategy

* recreate: deletes all pods before creating new
* rolling update: all traffic is sent to old version while new is scaling. After new is up-and-running, it will forward traffic to it.

* in `.yaml` you can customize:

```
strategy:
	type: RollingUpdate
	rollingUpdate:
		maxUnavailable: 1
		maxSurge: 1
```

* to pause deployment in the process: `kubectl rollout status deploy/deploy_name`
	- to resume if after: `kubectl rollout resume deploy/deploy_name`

### Services

* *port-forwarding* only for testing purposes.
* in production we need to use *services*

1. Client wants to connect to the pod. But pods are short-lived => IP addresses are constantly changing.
2. Instead we use *services*. Service - object in K8S.
3. Service has stable IP address which never changes. And Service is connected to the pods. Service has stable *DNS name* and *stable port*.

```
client -> SVC -> deployment [ReplicaSet [pod1, pod2, pod3]]
```

Types of services:
- ClusterIP (Default)
- NodePort
- ExternalName
- LoadBalancer

### ClusterIP

**Crucial:** `containerPort` must match port of the application server

```
pod [customer microservice] -> SVC [order service] -> pod1 pod2 pod3 [order microservice]
```
In this case our port and DNS stays the same (of SVC).

- `ClusterIP` is only for internal communication
- `Endpoints aka EP:` in `kubectl describe service order` are healthy endpoints of the pods == `kubectl get ep`

* With ClusterIP we have only internal IP without External Port  

```
write service IP + port for another service in customer pod (look at the diagram above) -> links to another service which maps to deployment with pods
```

* `kubectl port-forward deploy/customer 8080:8080` where first port is host and second port is container
* Apart from service IP, we have service DNS. Look in `customer-deployment.yaml` -> `value: "order:port"`
	- this is possible due to *service-discovery*
* we can remove port from above container: `value: "order"` and in the receiving container we put `80`

### NodePort

- allows us to open port on all nodes

```
cluster with 2 nodes: 192.168.49.2:30001 and 192.168.49.3:30001
where we also have svc.

Request -> node (IP + port) -> svc -> pod in this node

This svc will handle the request to the specified node or if it is unavailable - move request to another node
```

* Disadvantages:
	- one service per port
	- if node IP address gets changed, then it is a **problem**

- Look in customer-deployment for `service` category where you can find more

First variant: accessing via Node
* `minikube ssh` - to enter into the node. Use `-n` to specify the node: `minikube ssh -n minikube-m02`
	- `curl localhost:30002/api/v1/customer` - to send request using nodePort (look, 30002 is the one in `customer-deployment.yaml`).
		Plus, we can send to the IP of any node (as NodePort opens up ports on all nodes): `curl 192.168.49.3:30002/api/v1/customer`

Second variant: accessing directly via Service
* `minikube service service-name` - to access via the service directly, not the node as before.
	- it opens the browser and: COMMON_PART

* `minikube service service-name --url` to show the url. Take this and add ending for the desired API. As here we go through service.
	- COMMON_PART  

COMMON_PART: Here we do access the SERVICE directly, not NODE. `http://127.0.0.1:59778/api/v1/customer` - before `api` is what we receive from the command line and then we add the desired API.

Accessing locally
- ssh into pod. Then use `curl` to send request to another svc of ClusterIP type: `curl customer/api/v1/customer` (or we can use IP address of the ClusterIP)

### Load Balancer

- exposing applications to the internet. It creates load balancer per `svc`.
	if more than one svc - separate load balancer
- How it works on the cloud vs local:
	* NLB - network load balancer for cloud providers
	* MINIKUBE - command `minikube tunnel` if locally. Then, check `kubectl get svc` to observe how our LB svc get **EXTERNAL-IP** assigned 

- Cloud Controller Manager: responsible for interacting with the underlying cloud provider
	* I.e. if we run on AWS, it creates NLB on AWS
	* `api -> c-c-m -> cloud provider`

After we have applied load-balancer service we will have *pending* as we need to do *minikube tunnel*. And then we will have external IP address

* `kubernetes` service is created automatically so that we can find it and talk to the K8S API within the app and
	- https://stackoverflow.com/a/52860430/16543524
	- `kubectl get ep` to look at all endpoints of the pods which are attached to the svc. We can find the IP and PORT for `kubernetes` pod
	- `kubectl get pod -A` where you can find `kube-apiserver-minikube` pod
	- `kubectl describe pod kube-apiserver-minikube -n kube-system` to see this pod specs
	kubernetes service -> kubernetes pod (endpoint of the pod)

### Labels, Selectors, Annotations

**labels** are attached to objects (i.e. pods, services, rs etc). We can select/organize objects based on labels
**matchLabels** is for ReplicaSet to know which labels to use (so, to apply RS specifications). If nothing - applied to all

Here we specified 4 labels
```
  template:
    metadata:
      labels: # these 4 are all labels
        app: customer-java-deploy # RS from selector will be tied to each pod with this label associated with it
        environment: test # just to specify which environment is used (QA, production etc)
        tier: backend
        department: engineering
```

Useful command: `kubectl get pods --show-labels`

selectors - things which are used to filter K8S objects. RS, deploy all use selectors. Under the hood it uses boolean expressions.
Example: svc uses `selector` to match `labels` of the pods. Then *replica set* uses `selector` to select `labels` from pods

* `kubectl get pods --selector="department=engineering"` - to select pods based on selectors
* `kubectl get pods -l tier=backend,environment=test` - just another way to select based on selector
* set-based requirement: `kubectl get po -l 'environment in (test),tier in (backend)'`

`selector` is all-or-nothing. It means that if at least one selector doesn't match the labels - nothing is chosen. But if not all `labels` are specified in the `selector` - nothing wrong

Next, look that `selector` of svc chooses only matching pods by `labels` (first two) regardless of `name` field (it can be any gibberish)

For svc:
```
spec:
  selector:
    name: blue
```

For pods:

first:
```
apiVersion: v1
kind: Pod
metadata:
  name: green
  labels:
    name: blue
```

second:
```
apiVersion: v1
kind: Pod
metadata:
  name: blue
  labels:
    name: blue
```

third:
```
apiVersion: v1
kind: Pod
metadata:
  name: hello-world
  labels: 
    name: hello-world
```

So, if we add another *label* to the `labels` and not add to the pod - pod won't be included
```
spec:
  selector:
    name: blue
    environment: test
```

But recall that it is better to use deployment. Here we see that `metadata` and `labels` are also present

```
apiVersion: apps/v1
kind: Deployment
metadata:
  name: customer-java-deploy
spec:
  replicas: 2
  selector:
    matchLabels:
      app: customer-java-deploy
  template:
    metadata:
      labels:
        app: customer-java-deploy
        environment: test
        tier: backend
        department: engineering
```
