package com.github.fk.common.support.inteceptor;

public interface RpcInterceptor {

        /**
         * 开始
         * @param context 上下文
         * @since 0.1.4
         */
        void before(final RpcInterceptorContext context);

        /**
         * 结束
         * @param context 上下文
         * @since 0.1.4
         */
        void after(final RpcInterceptorContext context);

        /**
         * 异常处理
         * @param context 上下文
         * @since 0.1.4
         */
        void exception(final RpcInterceptorContext context);
}
