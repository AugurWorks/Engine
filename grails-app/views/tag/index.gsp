<!DOCTYPE html>
<html>
    <head>
        <title>Tags</title>
        <meta name="layout" content="semantic">
    </head>
    <body>
        <div class="ui segment">
            <h1 class="ui dividing header">Tags</h1>
            <div class="ui three cards">
                <g:each in="${ tags }" var="tag">
                    <g:link class="card" controller="tag" action="show" params="[tag: tag.name]">
                        <div class="content">
                            <div class="header">${ tag.name }</div>
                            <div class="description">
                                <ul>
                                    <g:each in="${ tag.requests }" var="request">
                                        <li>${ request.name }</li>
                                    </g:each>
                                </ul>
                            </div>
                        </div>
                    </g:link>
                </g:each>
            </div>
        </div>
    </body>
</html>
