#!/bin/bash

set -o errexit

function main() {
    CONTAINERS=$(docker container ls -aq)
    IMAGES=$(docker image ls -aq)

    CONTAINERS_LIST=$(echo $CONTAINERS | tr '\n' ' ')
    IMAGES_LIST=$(echo $IMAGES | tr '\n' ' ')

    echo "Containers: $CONTAINERS_LIST"
    echo "Imagens: $IMAGES_LIST"

    remove $CONTAINERS_LIST $IMAGES_LIST
    # prune 
    # check
}

function remove() {
    CONTAINERS=${1:-$(docker container ls -aq)}
    IMAGES=${2:-$(docker image ls -aq)}

    if [[ -n "$CONTAINERS" ]]; then
        echo "Stopping and removing containers..."
        docker container stop $CONTAINERS
        docker container rm $CONTAINERS
    fi

    if [[ -n "$IMAGES" ]]; then
        echo "Removing images..."
        docker image rm $IMAGES
    fi
}

function prune() {
    docker system prune --all --force
    docker container prune --force
    docker image prune --force
    docker volume prune --force
    docker network prune --force
}

function check() {
    docker container ls -a
    docker image ls -a
    docker volume ls
    docker network ls
}

main "$@"
