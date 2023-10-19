tasks.publishToMavenLocal {
    dependsOn(":src:compiler:core:publishToMavenLocal")
    dependsOn(":src:openapi:publishToMavenLocal")
}
