//
// Created by shwei on 2020/11/29.
//
#include <stdio.h>
#include <stdlib.h>

#include <libandroid_shm.h>

int main(int argc, char **argv)
{
    U64  ufd;
    char* buf;
    if (argc<=1) {
        ufd = 0;
        printf("LINE: %d\n", __LINE__);
        int ret = create_shared_memory("loctag", 1024, -1, buf, ufd);
        printf("LINE: %d\n", __LINE__);
        strcpy(buf, "shared_test1你好");
        printf("LINE: %d\n", __LINE__);

        open_shared_memory("loctag", -1, buf, ufd);
        printf("LINE: %d\n", __LINE__);
        char c[40];
        sprintf(c,"getDot11Rt %d %s", (int)ufd, buf);
        printf("LINE: %d %s\n", __LINE__, c);
    } else {
        ufd= (U64)atoi(argv[2]);
        perror("open_shared_memory1");
        int ret = open_shared_memory("loctag", -1, buf, ufd);
        perror("open_shared_memory2");
        printf("ret: %d, fd: %d\n", ret, (int)ufd);
        strcpy(buf, "shared_test2你好");
        printf("LINE: %d\n", __LINE__);
    }

    sp<IServiceManager> sm = defaultServiceManager();
    sp<IBinder> binder = sm->getService(String16("LoctagBinder"));
    // TODO: If the "Demo" service is not running, getService times out and binder == 0.
    ASSERT(binder != 0);
    sp<IDemo> loctagBinder = interface_cast<IDemo>(binder);
    ASSERT(demo != 0);
    return demo;

    return 0;
}

// Interface (our AIDL) - Shared by server and client
class ILoctag : public IInterface {
    public:
        enum {
            ALERT = IBinder::FIRST_CALL_TRANSACTION,
            PUSH,
            ADD
        };
        // Sends a user-provided value to the service
        virtual void        push(int32_t data)          = 0;
        // Sends a fixed alert string to the service
        virtual void        alert()                     = 0;
        // Requests the service to perform an addition and return the result
        virtual int32_t     add(int32_t v1, int32_t v2) = 0;

        DECLARE_META_INTERFACE(Demo);  // Expands to 5 lines below:
        //static const android::String16 descriptor;
        //static android::sp<IDemo> asInterface(const android::sp<android::IBinder>& obj);
        //virtual const android::String16& getInterfaceDescriptor() const;
        //IDemo();
        //virtual ~IDemo();
};

// Client
class BpDemo : public BpInterface<IDemo> {
    public:
        BpDemo(const sp<IBinder>& impl) : BpInterface<IDemo>(impl) {
            ALOGD("BpDemo::BpDemo()");
        }

        virtual void push(int32_t push_data) {
            Parcel data, reply;
            data.writeInterfaceToken(IDemo::getInterfaceDescriptor());
            data.writeInt32(push_data);

            aout << "BpDemo::push parcel to be sent:\n";
            data.print(PLOG); endl(PLOG);

            remote()->transact(PUSH, data, &reply);

            aout << "BpDemo::push parcel reply:\n";
            reply.print(PLOG); endl(PLOG);

            ALOGD("BpDemo::push(%i)", push_data);
        }

        virtual void alert() {
            Parcel data, reply;
            data.writeInterfaceToken(IDemo::getInterfaceDescriptor());
            data.writeString16(String16("The alert string"));
            remote()->transact(ALERT, data, &reply, IBinder::FLAG_ONEWAY);    // asynchronous call
            ALOGD("BpDemo::alert()");
        }

        virtual int32_t add(int32_t v1, int32_t v2) {
            Parcel data, reply;
            data.writeInterfaceToken(IDemo::getInterfaceDescriptor());
            data.writeInt32(v1);
            data.writeInt32(v2);
            aout << "BpDemo::add parcel to be sent:\n";
            data.print(PLOG); endl(PLOG);
            remote()->transact(ADD, data, &reply);
            ALOGD("BpDemo::add transact reply");
            reply.print(PLOG); endl(PLOG);

            int32_t res;
            status_t status = reply.readInt32(&res);
            ALOGD("BpDemo::add(%i, %i) = %i (status: %i)", v1, v2, res, status);
            return res;
        }
};

    //IMPLEMENT_META_INTERFACE(Demo, "Demo");
    // Macro above expands to code below. Doing it by hand so we can log ctor and destructor calls.
    const android::String16 IDemo::descriptor("Demo");
    const android::String16& IDemo::getInterfaceDescriptor() const {
        return IDemo::descriptor;
    }
    android::sp<IDemo> IDemo::asInterface(const android::sp<android::IBinder>& obj) {
        android::sp<IDemo> intr;
        if (obj != NULL) {
            intr = static_cast<IDemo*>(obj->queryLocalInterface(IDemo::descriptor).get());
            if (intr == NULL) {
                intr = new BpDemo(obj);
            }
        }
        return intr;
    }
    IDemo::IDemo() { ALOGD("IDemo::IDemo()"); }
    IDemo::~IDemo() { ALOGD("IDemo::~IDemo()"); }