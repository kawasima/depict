all: pom examples

check-version:
ifndef VERSION
	$(error VERSION is not set)
endif

pom: check-version
	mvn versions:set -DnewVersion=${VERSION}

gradle: check-version
	cd depict-gradle-plugin && \
		perl -pe "s/^version\s*=\s*['\"]([^'\"]+)['\"]/version = '${VERSION}'/" build.gradle

examples: example-pom example-gradle

example-pom: check-version
	cd depict-example && mvn versions:set-property -DnewVersion=${VERSION} -Dproperty=depict.version

example-gradle: check-version
	cd depict-example && \
		perl -i -pe "s/id ['\"]net.unit8.depict['\"] version ['\"]([^'\"]+)['\"]/id 'net.unit8.depict' version '${VERSION}'/g;s/['\"]net.unit8.depict:depict-gradle-plugin:([^'\"]+)['\"]/\"net.unit8.depict:depict-gradle-plugin:${VERSION}\"/g" *.gradle **/*.gradle
