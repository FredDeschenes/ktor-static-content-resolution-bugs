This demonstrates 2 bugs regarding static content resolution in Ktor.

# First issue, running from an IDE

Run `Main.kt` in an IDE
 
```
$ curl -i localhost:8080/
HTTP/1.1 500 Internal Server Error
Content-Length: 0
```

Stack :
```
[nettyCallPool-4-1] ERROR ktor.application - Unhandled: GET - /
java.io.FileNotFoundException: C:\Users\fDeschenes\Desktop\static-content-resolution-bugs\out\production\resources\static (Permission denied)
	at java.io.RandomAccessFile.open0(Native Method)
	at java.io.RandomAccessFile.open(RandomAccessFile.java:316)
	at java.io.RandomAccessFile.<init>(RandomAccessFile.java:243)
	at io.ktor.cio.FileChannelsKt.readChannel(FileChannels.kt:18)
	at io.ktor.cio.FileChannelsKt.readChannel$default(FileChannels.kt:15)
	at io.ktor.content.LocalFileContent.readFrom(LocalFileContent.kt:29)
	at io.ktor.server.engine.BaseApplicationResponse.respondOutgoingContent$suspendImpl(BaseApplicationResponse.kt:118)
	at io.ktor.server.engine.BaseApplicationResponse.respondOutgoingContent(BaseApplicationResponse.kt)
	at io.ktor.server.netty.NettyApplicationResponse.respondOutgoingContent$suspendImpl(NettyApplicationResponse.kt:34)
	at io.ktor.server.netty.NettyApplicationResponse.respondOutgoingContent(NettyApplicationResponse.kt)
	at io.ktor.server.engine.BaseApplicationResponse$$special$$inlined$apply$lambda$1.doResume(BaseApplicationResponse.kt:35)
	at io.ktor.server.engine.BaseApplicationResponse$$special$$inlined$apply$lambda$1.invoke(BaseApplicationResponse.kt)
	at io.ktor.server.engine.BaseApplicationResponse$$special$$inlined$apply$lambda$1.invoke(BaseApplicationResponse.kt:16)
	at io.ktor.pipeline.PipelineContext.proceed(PipelineContext.kt:49)
	at io.ktor.pipeline.PipelineContext.proceedWith(PipelineContext.kt:35)
	at io.ktor.server.engine.DefaultTransformKt$installDefaultTransformations$1.doResume(DefaultTransform.kt:22)
	at io.ktor.server.engine.DefaultTransformKt$installDefaultTransformations$1.invoke(DefaultTransform.kt)
	at io.ktor.server.engine.DefaultTransformKt$installDefaultTransformations$1.invoke(DefaultTransform.kt)
	at io.ktor.pipeline.PipelineContext.proceed(PipelineContext.kt:49)
	at io.ktor.pipeline.Pipeline.execute(Pipeline.kt:22)
	at io.ktor.content.StaticContentKt$resources$1.doResume(StaticContent.kt:153)
	at io.ktor.content.StaticContentKt$resources$1.invoke(StaticContent.kt)
	at io.ktor.content.StaticContentKt$resources$1.invoke(StaticContent.kt)
	at io.ktor.pipeline.PipelineContext.proceed(PipelineContext.kt:49)
	at io.ktor.pipeline.Pipeline.execute(Pipeline.kt:22)
	at io.ktor.routing.Routing.executeResult(Routing.kt:100)
	at io.ktor.routing.Routing.interceptor(Routing.kt:25)
	at io.ktor.routing.Routing$Feature$install$1.doResume(Routing.kt:66)
	at io.ktor.routing.Routing$Feature$install$1.invoke(Routing.kt)
	at io.ktor.routing.Routing$Feature$install$1.invoke(Routing.kt:51)
	at io.ktor.pipeline.PipelineContext.proceed(PipelineContext.kt:49)
	at io.ktor.pipeline.Pipeline.execute(Pipeline.kt:22)
	at io.ktor.server.engine.DefaultEnginePipelineKt$defaultEnginePipeline$2.doResume(DefaultEnginePipeline.kt:66)
	at io.ktor.server.engine.DefaultEnginePipelineKt$defaultEnginePipeline$2.invoke(DefaultEnginePipeline.kt)
	at io.ktor.server.engine.DefaultEnginePipelineKt$defaultEnginePipeline$2.invoke(DefaultEnginePipeline.kt)
	at io.ktor.pipeline.PipelineContext.proceed(PipelineContext.kt:49)
	at io.ktor.pipeline.Pipeline.execute(Pipeline.kt:22)
	at io.ktor.server.netty.NettyApplicationCallHandler$handleRequest$1.doResume(NettyApplicationCallHandler.kt:31)
	at io.ktor.server.netty.NettyApplicationCallHandler$handleRequest$1.invoke(NettyApplicationCallHandler.kt)
	at io.ktor.server.netty.NettyApplicationCallHandler$handleRequest$1.invoke(NettyApplicationCallHandler.kt:10)
	at kotlinx.coroutines.experimental.intrinsics.UndispatchedKt.startCoroutineUndispatched(Undispatched.kt:44)
	at kotlinx.coroutines.experimental.CoroutineStart.invoke(CoroutineStart.kt:113)
	at kotlinx.coroutines.experimental.AbstractCoroutine.start(AbstractCoroutine.kt:165)
	at kotlinx.coroutines.experimental.BuildersKt__Builders_commonKt.launch(Builders.common.kt:72)
	at kotlinx.coroutines.experimental.BuildersKt.launch(Unknown Source)
	at kotlinx.coroutines.experimental.BuildersKt__Builders_commonKt.launch$default(Builders.common.kt:64)
	at kotlinx.coroutines.experimental.BuildersKt.launch$default(Unknown Source)
	at io.ktor.server.netty.NettyApplicationCallHandler.handleRequest(NettyApplicationCallHandler.kt:22)
	at io.ktor.server.netty.NettyApplicationCallHandler.channelRead(NettyApplicationCallHandler.kt:16)
	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:362)
	at io.netty.channel.AbstractChannelHandlerContext.access$600(AbstractChannelHandlerContext.java:38)
	at io.netty.channel.AbstractChannelHandlerContext$7.run(AbstractChannelHandlerContext.java:353)
	at io.netty.util.concurrent.AbstractEventExecutor.safeExecute(AbstractEventExecutor.java:163)
	at io.netty.util.concurrent.SingleThreadEventExecutor.runAllTasks(SingleThreadEventExecutor.java:404)
	at io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:463)
	at io.netty.util.concurrent.SingleThreadEventExecutor$5.run(SingleThreadEventExecutor.java:884)
	at io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30)
	at java.lang.Thread.run(Thread.java:748)
```

Looks like this happens because no check is done in `ApplicationCall.resolveResource` (in the "file" case) to validate
that we are accessing a file and not a directory.

# Second issue, running from a JAR

```shell
$ ./gradlew shadowJar
$ java -cp build/libs/static-content-resolution-bugs-1.0-SNAPSHOT-all.jar MainKt
...
$ curl -i localhost:8080
HTTP/1.1 500 Internal Server Error
Content-Length: 0
```

Stack:
```
[nettyCallPool-4-1] ERROR ktor.application - Unhandled: GET - /
java.lang.StringIndexOutOfBoundsException: String index out of range: -1
        at java.lang.String.substring(String.java:1927)
        at io.ktor.content.StaticContentResolutionKt.extension(StaticContentResolution.kt:58)
        at io.ktor.content.StaticContentResolutionKt.resolveResource(StaticContentResolution.kt:34)
        at io.ktor.content.StaticContentResolutionKt.resolveResource$default(StaticContentResolution.kt:19)
        at io.ktor.content.StaticContentKt$resources$1.doResume(StaticContent.kt:132)
        at io.ktor.content.StaticContentKt$resources$1.invoke(StaticContent.kt)
        at io.ktor.content.StaticContentKt$resources$1.invoke(StaticContent.kt)
        at io.ktor.pipeline.PipelineContext.proceed(PipelineContext.kt:49)
        at io.ktor.pipeline.Pipeline.execute(Pipeline.kt:22)
        at io.ktor.routing.Routing.executeResult(Routing.kt:100)
        at io.ktor.routing.Routing.interceptor(Routing.kt:25)
        at io.ktor.routing.Routing$Feature$install$1.doResume(Routing.kt:66)
        at io.ktor.routing.Routing$Feature$install$1.invoke(Routing.kt)
        at io.ktor.routing.Routing$Feature$install$1.invoke(Routing.kt:51)
        at io.ktor.pipeline.PipelineContext.proceed(PipelineContext.kt:49)
        at io.ktor.pipeline.Pipeline.execute(Pipeline.kt:22)
        at io.ktor.server.engine.DefaultEnginePipelineKt$defaultEnginePipeline$2.doResume(DefaultEnginePipeline.kt:66)
        at io.ktor.server.engine.DefaultEnginePipelineKt$defaultEnginePipeline$2.invoke(DefaultEnginePipeline.kt)
        at io.ktor.server.engine.DefaultEnginePipelineKt$defaultEnginePipeline$2.invoke(DefaultEnginePipeline.kt)
        at io.ktor.pipeline.PipelineContext.proceed(PipelineContext.kt:49)
        at io.ktor.pipeline.Pipeline.execute(Pipeline.kt:22)
        at io.ktor.server.netty.NettyApplicationCallHandler$handleRequest$1.doResume(NettyApplicationCallHandler.kt:31)
        at io.ktor.server.netty.NettyApplicationCallHandler$handleRequest$1.invoke(NettyApplicationCallHandler.kt)
        at io.ktor.server.netty.NettyApplicationCallHandler$handleRequest$1.invoke(NettyApplicationCallHandler.kt:10)
        at kotlinx.coroutines.experimental.intrinsics.UndispatchedKt.startCoroutineUndispatched(Undispatched.kt:44)
        at kotlinx.coroutines.experimental.CoroutineStart.invoke(CoroutineStart.kt:113)
        at kotlinx.coroutines.experimental.AbstractCoroutine.start(AbstractCoroutine.kt:165)
        at kotlinx.coroutines.experimental.BuildersKt__Builders_commonKt.launch(Builders.common.kt:72)
        at kotlinx.coroutines.experimental.BuildersKt.launch(Unknown Source)
        at kotlinx.coroutines.experimental.BuildersKt__Builders_commonKt.launch$default(Builders.common.kt:64)
        at kotlinx.coroutines.experimental.BuildersKt.launch$default(Unknown Source)
        at io.ktor.server.netty.NettyApplicationCallHandler.handleRequest(NettyApplicationCallHandler.kt:22)
        at io.ktor.server.netty.NettyApplicationCallHandler.channelRead(NettyApplicationCallHandler.kt:16)
        at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:362)
        at io.netty.channel.AbstractChannelHandlerContext.access$600(AbstractChannelHandlerContext.java:38)
        at io.netty.channel.AbstractChannelHandlerContext$7.run(AbstractChannelHandlerContext.java:353)
        at io.netty.util.concurrent.AbstractEventExecutor.safeExecute(AbstractEventExecutor.java:163)
        at io.netty.util.concurrent.SingleThreadEventExecutor.runAllTasks(SingleThreadEventExecutor.java:404)
        at io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:463)
        at io.netty.util.concurrent.SingleThreadEventExecutor$5.run(SingleThreadEventExecutor.java:884)
        at io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30)
        at java.lang.Thread.run(Thread.java:748)
```

This happens because there is no range-check in the `String.extension` extension function (basically it explodes when
checking a resource without an extension).