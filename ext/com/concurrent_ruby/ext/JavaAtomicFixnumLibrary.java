package com.concurrent_ruby.ext;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;
import org.jruby.Ruby;
import org.jruby.RubyBoolean;
import org.jruby.RubyClass;
import org.jruby.RubyFixnum;
import org.jruby.RubyModule;
import org.jruby.RubyObject;
import org.jruby.anno.JRubyClass;
import org.jruby.anno.JRubyMethod;
import org.jruby.runtime.ObjectAllocator;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.runtime.load.Library;

public class JavaAtomicFixnumLibrary implements Library {

    public void load(Ruby runtime, boolean wrap) throws IOException {
        RubyModule concurrentMod = runtime.defineModule("Concurrent");
        RubyClass atomicCls = concurrentMod.defineClassUnder("JavaAtomicFixnum", runtime.getObject(), JRUBYREFERENCE_ALLOCATOR);

        atomicCls.defineAnnotatedMethods(JavaAtomicFixnum.class);

    }

    private static final ObjectAllocator JRUBYREFERENCE_ALLOCATOR = new ObjectAllocator() {
        public IRubyObject allocate(Ruby runtime, RubyClass klazz) {
            return new JavaAtomicFixnum(runtime, klazz);
        }
    };

    @JRubyClass(name = "JavaAtomicFixnum", parent = "Object")
    public static class JavaAtomicFixnum extends RubyObject {

        private AtomicLong atomicLong;
        private ThreadContext context;

        public JavaAtomicFixnum(Ruby runtime, RubyClass metaClass) {
            super(runtime, metaClass);
        }

        @JRubyMethod
        public IRubyObject initialize(ThreadContext context) {
            this.atomicLong = new AtomicLong(0);
            this.context = context;
            return context.nil;
        }

        @JRubyMethod
        public IRubyObject initialize(ThreadContext context, IRubyObject value) {
            this.atomicLong = new AtomicLong(rubyFixnumToLong(value));
            this.context = context;
            return context.nil;
        }

        @JRubyMethod(name = "value")
        public IRubyObject getValue() {
            return new RubyFixnum(getRuntime(), atomicLong.get());
        }

        @JRubyMethod(name = "value=")
        public IRubyObject setValue(IRubyObject newValue) {
            atomicLong.set(rubyFixnumToLong(newValue));
            return context.nil;
        }

        @JRubyMethod(name = {"increment", "up"})
        public IRubyObject increment() {
            return new RubyFixnum(getRuntime(), atomicLong.incrementAndGet());
        }

        @JRubyMethod(name = {"decrement", "down"})
        public IRubyObject decrement() {
            return new RubyFixnum(getRuntime(), atomicLong.decrementAndGet());
        }

        @JRubyMethod(name = "compare_and_set")
        public IRubyObject compareAndSet(IRubyObject expect, IRubyObject update) {
            return RubyBoolean.newBoolean(getRuntime(), atomicLong.compareAndSet(rubyFixnumToLong(expect), rubyFixnumToLong(update)));
        }

        private long rubyFixnumToLong(IRubyObject value) {
            if (value instanceof RubyFixnum) {
                RubyFixnum fixNum = (RubyFixnum) value;
                return fixNum.getLongValue();
            } else {
                throw getRuntime().newArgumentError("initial value must be a Fixnum");
            }
        }
    }
}

